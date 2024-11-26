package com.example.matdongsanserver.domain.auth.jwt;

import com.example.matdongsanserver.domain.auth.kakao.KakaoMemberDetails;
import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshToken;
import com.example.matdongsanserver.domain.auth.jwt.redis.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final String AUTH_ID = "ID";
    private static final String AUTH_KEY = "AUTHORITY";
    private static final String AUTH_EMAIL = "EMAIL";

    private final String secretKey;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final RefreshTokenRepository refreshTokenRepository;

    private Key secretkey;

    public TokenProvider(@Value("${spring.jwt.secret_key}") String secretKey,
                         @Value("${spring.jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
                         @Value("${spring.jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime,
                         RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = secretKey;
        this.accessTokenValidityTime = accessTokenValidityTime * 1000;
        this.refreshTokenValidityTime = refreshTokenValidityTime * 1000;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretkey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * access, refresh Token 생성
     */
    public TokenDto createToken(Long memberId, String email, String role) {
        long now = (new Date()).getTime();

        Date accessValidity = new Date(now + this.accessTokenValidityTime);
        Date refreshValidity = new Date(now + this.refreshTokenValidityTime);

        String accessToken = Jwts.builder()
                .addClaims(Map.of(AUTH_ID, memberId))
                .addClaims(Map.of(AUTH_EMAIL, email))
                .addClaims(Map.of(AUTH_KEY, role))
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .setExpiration(accessValidity)
                .compact();

        String refreshToken = Jwts.builder()
                .addClaims(Map.of(AUTH_ID, memberId))
                .addClaims(Map.of(AUTH_EMAIL, email))
                .addClaims(Map.of(AUTH_KEY, role))
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .setExpiration(refreshValidity)
                .compact();

        return TokenDto.of(accessToken, refreshToken);
    }

    /**
     * 토큰의 유효성 검사
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException | UnsupportedJwtException e) {
            return false;
        }
    }

    /**
     * 토큰이 만료되었는지 검사
     */
    public boolean validateExpire(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 Authentication 객체를 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        List<String> authorities = Arrays.asList(claims.get(AUTH_KEY)
                .toString()
                .split(","));

        List<? extends GrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth))
                .collect(Collectors.toList());

        KakaoMemberDetails principal = new KakaoMemberDetails(
                (Long) claims.get(AUTH_ID),
                (String) claims.get(AUTH_EMAIL),
                simpleGrantedAuthorities, Map.of());

        return new UsernamePasswordAuthenticationToken(principal, token, simpleGrantedAuthorities);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public TokenDto reissueAccessToken(String refreshToken) {
        RefreshToken findToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        TokenDto tokenDto = createToken(findToken.getId(), findToken.getEmail(), findToken.getAuthority());
        refreshTokenRepository.save(RefreshToken.builder()
                .id(findToken.getId())
                .email(findToken.getEmail())
                .authorities(findToken.getAuthorities())
                .refreshToken(tokenDto.getRefreshToken())
                .build());

        return tokenDto;
    }
}