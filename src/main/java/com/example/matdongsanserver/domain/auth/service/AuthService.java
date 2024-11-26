package com.example.matdongsanserver.domain.auth.service;

import com.example.matdongsanserver.domain.auth.dto.KakaoLoginRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인증 코드를 통해서 로그인을 수행하는 로직
     * @param code
     * @return LoginResponse
     * @throws JsonProcessingException
     */
    @Transactional
    public LoginResponse kakaoLogin(final String code) throws JsonProcessingException {
        String token = getToken(code);
        System.out.println(token);
        KakaoLoginRequest request = getKakaoUserInfo(token);

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseGet(() ->
                        memberRepository.save(
                                Member.builder()
                                        .email(request.getEmail())
                                        .nickname(request.getNickname())
                                        .profileImage(request.getProfileImage())
                                        .role(Role.USER)
                                        .build()
                        )
                );

        TokenResponse tokenResponse = tokenProvider.createToken(member.getId(), member.getEmail(), member.getRole().name());

        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(member.getRole().name()));

        refreshTokenRepository.save(RefreshToken.builder()
                .id(member.getId())
                .email(member.getEmail())
                .authorities(simpleGrantedAuthorities)
                .refreshToken(tokenResponse.getRefreshToken())
                .build());

        if (member.hasNoChildren()) {
            return LoginResponse.of(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), true);
        }

        return LoginResponse.of(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), false);
    }

    /**
     * 인증 서버로부터 인증 코드를 통해서 access 토큰을 받아오는 로직
     * @param code
     * @return access 토큰
     * @throws JsonProcessingException
     */
    private String getToken(final String code) throws JsonProcessingException {
        // HTTP 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 바디 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        String responseBody = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("access_token").asText();
    }

    /**
     * 인증 서버로부터 access 토큰을 통해서 사용자 정보를 받아오는 로직
     * @param token
     * @return KakaoLoginRequest
     * @throws JsonProcessingException
     */
    private KakaoLoginRequest getKakaoUserInfo(final String token) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 이메일, 닉네임, 프로필 이미지 추출
            String email = jsonNode.get("kakao_account").get("email").asText();
            String nickname = jsonNode.get("kakao_account").get("profile").get("nickname").asText();
            String profileImage = jsonNode.get("kakao_account").get("profile").get("profile_image_url").asText();

            return new KakaoLoginRequest(email, nickname, profileImage);
        } catch (HttpClientErrorException e) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }
    }
}
