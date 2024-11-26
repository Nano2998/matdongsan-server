//package com.example.matdongsanserver.domain.auth.handler;
//
//import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
//import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
//import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshToken;
//import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshTokenRepository;
//import com.example.matdongsanserver.domain.auth.kakao.KakaoUserInfo;
//import com.example.matdongsanserver.domain.member.entity.Member;
//import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
//import com.example.matdongsanserver.domain.member.exception.MemberException;
//import com.example.matdongsanserver.domain.member.repository.MemberRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final TokenProvider tokenProvider;
//    private final MemberRepository memberRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//
//    @Transactional
//    @Override
//    public void onAuthenticationSuccess(
//            HttpServletRequest request, HttpServletResponse response, Authentication authentication
//    ) throws IOException, ServletException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
//
//        Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail())
//                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
//
//        TokenResponse tokenResponse = tokenProvider.createToken(member.getId(), member.getEmail(), member.getRole().name());
//
//        saveRefreshTokenOnRedis(member, tokenResponse);
//        writeTokenResponse(response, tokenResponse);
//    }
//
//    private void saveRefreshTokenOnRedis(Member member, TokenResponse tokenResponse) {
//        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
//        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(member.getRole().name()));
//        refreshTokenRepository.save(RefreshToken.builder()
//                .id(member.getId())
//                .email(member.getEmail())
//                .authorities(simpleGrantedAuthorities)
//                .refreshToken(tokenResponse.getRefreshToken())
//                .build());
//    }
//
//    private void writeTokenResponse(HttpServletResponse response, TokenResponse tokenResponse) throws IOException {
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.writeValue(response.getWriter(), tokenResponse);
//    }
//}
