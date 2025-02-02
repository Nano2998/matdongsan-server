package com.example.matdongsanserver.domain.auth.service;

import com.example.matdongsanserver.domain.auth.client.KakaoAuthClient;
import com.example.matdongsanserver.domain.auth.client.KakaoUserInfoClient;
import com.example.matdongsanserver.domain.auth.dto.KakaoInfo;
import com.example.matdongsanserver.domain.auth.dto.LoginRequest;
import com.example.matdongsanserver.domain.auth.dto.LoginResponse;
import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.exception.AuthException;
import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshToken;
import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshTokenRepository;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoUserInfoClient kakaoUserInfoClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private static final String REFRESH_HEADER = "refreshToken";

    /**
     * 인증 서버로부터 인증 코드를 통해 access 토큰을 받아오는 로직
     * @param code
     * @return
     */
    public String getToken(final String code) {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("grant_type", "authorization_code");
        requestParams.put("client_id", clientId);
        requestParams.put("client_secret", clientSecret);
        requestParams.put("redirect_uri", redirectUri);
        requestParams.put("code", code);

        try {
            ResponseEntity<String> response = kakaoAuthClient.getAccessToken(requestParams);

            return parseJsonNode(response.getBody()).get("access_token").asText();
        } catch (FeignException e) {
            throw new AuthException(AuthErrorCode.AUTH_SERVER_ERROR);
        }
    }

    /**
     * 카카오 로그인을 처리하는 로직
     * @param loginRequest
     * @return
     */
    @Transactional
    public LoginResponse kakaoLogin(LoginRequest loginRequest) {
        KakaoInfo kakaoInfo = getKakaoUserEmail(loginRequest.getToken());
        validateLoginRequest(kakaoInfo.getEmail(), loginRequest.getEmail());

        Member member = getOrRegisterMember(kakaoInfo);

        TokenResponse tokenResponse = tokenProvider.createToken(
                member.getId(), member.getEmail(), member.getRole().name());

        saveRefreshToken(member, tokenResponse.getRefreshToken());

        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .isChildRegistered(member.isChildRegistered())
                .build();
    }

    /**
     * 리프레시 토큰을 통해 새로운 엑세스, 리프레시 토큰을 발급
     * @param request
     * @return
     */
    @Transactional
    public TokenResponse reissueAccessToken(final HttpServletRequest request) {
        String refreshToken = getTokenFromHeader(request, REFRESH_HEADER);

        validateRefreshToken(refreshToken);

        RefreshToken findToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(
                        () ->  new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED)
                );

        TokenResponse tokenResponse = tokenProvider.createToken(
                findToken.getId(), findToken.getEmail(), findToken.getAuthority());

        updateRefreshToken(findToken, tokenResponse.getRefreshToken());

        SecurityContextHolder.getContext()
                .setAuthentication(tokenProvider.getAuthentication(tokenResponse.getAccessToken()));

        return tokenResponse;
    }

    /**
     * 카카오 서버에서 사용자 이메일 정보를 가져옴
     * @param token
     * @return
     */
    private KakaoInfo getKakaoUserEmail(final String token) {
        log.info("Retrieving Kakao user email.");
        try {
            ResponseEntity<String> response = kakaoUserInfoClient.getUserInfo("Bearer " + token);

            return KakaoInfo.builder()
                    .email(parseJsonNode(response.getBody()).get("kakao_account").get("email").asText())
                    .nickname(parseJsonNode(response.getBody()).get("kakao_account").get("profile").get("nickname").asText())
                    .profileImage(parseJsonNode(response.getBody()).get("kakao_account").get("profile").get("profile_image_url").asText())
                    .build();
        } catch (FeignException e) {
            throw new AuthException(AuthErrorCode.AUTH_SERVER_ERROR);
        }
    }

    /**
     * 리프레시 토큰 검증
     * @param refreshToken
     */
    private void validateRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || !tokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        if (!tokenProvider.validateTokenExpired(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * 헤더에서 토큰 추출
     * @param request
     * @param headerName
     * @return
     */
    private String getTokenFromHeader(final HttpServletRequest request, final String headerName) {
        String token = request.getHeader(headerName);
        if (!StringUtils.hasText(token)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return token;
    }

    /**
     * 요청에 대한 이메일 검증
     * @param actualEmail
     * @param expectedEmail
     */
    private void validateLoginRequest(String actualEmail, String expectedEmail) {
        if (!Objects.equals(actualEmail, expectedEmail)) {
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_REQUEST);
        }
    }

    /**
     * 회원을 찾거나 생성
     * @param kakaoInfo
     * @return
     */
    private Member getOrRegisterMember(KakaoInfo kakaoInfo) {
        return memberRepository.findByEmail(kakaoInfo.getEmail())
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email(kakaoInfo.getEmail())
                                .profileImage(kakaoInfo.getProfileImage())
                                .nickname(kakaoInfo.getNickname())
                                .role(Role.USER)
                                .build()));
    }

    /**
     * RefreshToken 엔티티 저장
     * @param member
     * @param refreshToken
     */
    private void saveRefreshToken(Member member, String refreshToken) {
        // 기존 토큰이 있다면 삭제
        refreshTokenRepository.deleteByEmail(member.getEmail());

        refreshTokenRepository.save(RefreshToken.builder()
                .id(member.getId())
                .email(member.getEmail())
                .authorities(List.of(new SimpleGrantedAuthority(member.getRole().name())))
                .refreshToken(refreshToken)
                .build());
    }

    /**
     * RefreshToken 엔티티 업데이트
     * @param findToken
     * @param newRefreshToken
     */
    private void updateRefreshToken(RefreshToken findToken, String newRefreshToken) {
        // 기존 토큰이 있다면 삭제
        refreshTokenRepository.delete(findToken);

        refreshTokenRepository.save(RefreshToken.builder()
                .id(findToken.getId())
                .email(findToken.getEmail())
                .authorities(findToken.getAuthorities())
                .refreshToken(newRefreshToken)
                .build());
    }

    /**
     * JSON 응답 파싱
     * @param json
     * @return
     */
    private JsonNode parseJsonNode(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new AuthException(AuthErrorCode.JSON_PARSING_ERROR);
        }
    }
}
