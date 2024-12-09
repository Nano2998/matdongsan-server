package com.example.matdongsanserver.domain.auth.service;

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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private static final String REFRESH_HEADER = "refreshToken";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * 인증 서버로부터 인증 코드를 통해 access 토큰을 받아오는 로직
     */
    public String getToken(final String code) throws JsonProcessingException {
        log.info("Attempting to retrieve Kakao access token for code: {}", code);
        HttpEntity<MultiValueMap<String, String>> request = buildKakaoTokenRequest(code);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL, HttpMethod.POST, request, String.class);

            log.info("Successfully retrieved Kakao access token.");
            return parseJsonNode(response.getBody()).get("access_token").asText();
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve Kakao access token: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.AUTH_SERVER_ERROR);
        }
    }

    /**
     * 카카오 로그인을 처리하는 로직
     */
    @Transactional
    public LoginResponse kakaoLogin(LoginRequest loginRequest) throws JsonProcessingException {
        log.info("Processing Kakao login for token.");
        KakaoInfo kakaoInfo = getKakaoUserEmail(loginRequest.getToken());
        validateLoginRequest(kakaoInfo.getEmail(), loginRequest.getEmail());

        Member member = findOrCreateMember(kakaoInfo);
        log.info("Member retrieved or created. memberId={}, email={}", member.getId(), member.getEmail());

        TokenResponse tokenResponse = tokenProvider.createToken(
                member.getId(), member.getEmail(), member.getRole().name());

        saveRefreshToken(member, tokenResponse.getRefreshToken());
        log.info("Token created for memberId: {}", member.getId());

        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .isChildRegistered(member.isChildRegistered())
                .build();
    }

    /**
     * 리프레시 토큰을 통해 새로운 엑세스, 리프레시 토큰을 발급
     */
    @Transactional
    public TokenResponse reissueAccessToken(final HttpServletRequest request) {
        log.info("Reissuing access and refresh tokens.");
        String refreshToken = getTokenFromHeader(request, REFRESH_HEADER);

        validateRefreshToken(refreshToken);

        RefreshToken findToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token has expired.");
                    return new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
                });

        TokenResponse tokenResponse = tokenProvider.createToken(
                findToken.getId(), findToken.getEmail(), findToken.getAuthority());
        log.info("New tokens created for memberId: {}", findToken.getId());

        updateRefreshToken(findToken, tokenResponse.getRefreshToken());

        SecurityContextHolder.getContext()
                .setAuthentication(tokenProvider.getAuthentication(tokenResponse.getAccessToken()));

        return tokenResponse;
    }

    /**
     * 카카오 서버에서 사용자 이메일 정보를 가져옴
     */
    private KakaoInfo getKakaoUserEmail(final String token) throws JsonProcessingException {
        log.info("Retrieving Kakao user email.");
        HttpEntity<MultiValueMap<String, String>> request = buildKakaoUserInfoRequest(token);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL, HttpMethod.POST, request, String.class);

            log.info("Successfully retrieved Kakao user email.");

            return KakaoInfo.builder()
                    .email(parseJsonNode(response.getBody()).get("kakao_account").get("email").asText())
                    .nickname(parseJsonNode(response.getBody()).get("kakao_account").get("profile").get("nickname").asText())
                    .profileImage(parseJsonNode(response.getBody()).get("kakao_account").get("profile").get("profile_image_url").asText())
                    .build();
        } catch (HttpClientErrorException e) {
            log.error("Failed to retrieve Kakao user email: {}", e.getMessage());
            throw new AuthException(AuthErrorCode.AUTH_SERVER_ERROR);
        }
    }

    /**
     * 리프레시 토큰 검증
     */
    private void validateRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || !tokenProvider.validateToken(refreshToken)) {
            log.warn("Invalid or empty refresh token.");
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        if (!tokenProvider.validateTokenExpired(refreshToken)) {
            log.warn("Refresh token has expired.");
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    /**
     * 헤더에서 토큰 추출
     */
    private String getTokenFromHeader(final HttpServletRequest request, final String headerName) {
        String token = request.getHeader(headerName);
        if (!StringUtils.hasText(token)) {
            log.warn("No token found in header: {}", headerName);
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return token;
    }

    /**
     * 요청에 대한 이메일 검증
     */
    private void validateLoginRequest(String actualEmail, String expectedEmail) {
        if (!Objects.equals(actualEmail, expectedEmail)) {
            log.warn("Email mismatch during login request. expected={}, actual={}", expectedEmail, actualEmail);
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_REQUEST);
        }
    }

    /**
     * 회원을 찾거나 생성
     */
    private Member findOrCreateMember(KakaoInfo kakaoInfo) {
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
     * 카카오 토큰 요청 빌더
     */
    private HttpEntity<MultiValueMap<String, String>> buildKakaoTokenRequest(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        return new HttpEntity<>(body, headers);
    }

    /**
     * 카카오 사용자 정보 요청 빌더
     */
    private HttpEntity<MultiValueMap<String, String>> buildKakaoUserInfoRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return new HttpEntity<>(headers);
    }

    /**
     * JSON 응답 파싱
     */
    private JsonNode parseJsonNode(String json) throws JsonProcessingException {
        return new ObjectMapper().readTree(json);
    }
}
