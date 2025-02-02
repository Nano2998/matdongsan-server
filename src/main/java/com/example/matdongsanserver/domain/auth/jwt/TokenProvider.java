package com.example.matdongsanserver.domain.auth.jwt;

import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
import com.example.matdongsanserver.domain.auth.kakao.KakaoMemberDetails;
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

    private Key secretkey;

    public TokenProvider(@Value("${spring.jwt.secret_key}") String secretKey,
                         @Value("${spring.jwt.access-token-validity-in-seconds}") long accessTokenValidityTime,
                         @Value("${spring.jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityTime
    ) {
        this.secretKey = secretKey;
        this.accessTokenValidityTime = accessTokenValidityTime * 1000;
        this.refreshTokenValidityTime = refreshTokenValidityTime * 1000;
    }

    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretkey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * access, refresh Token 생성
     * @param memberId
     * @param email
     * @param role
     * @return
     */
    public TokenResponse createToken(Long memberId, String email, String role) {
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

        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * 토큰의 유효성 검사
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된경우도 true 리턴
            return true;
        }
        catch (SecurityException | MalformedJwtException | IllegalArgumentException | UnsupportedJwtException e) {
            return false;
        }
    }

    /**
     * 토큰의 만료 여부 검사
     * @param token
     * @return
     */
    public boolean validateTokenExpired(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료 여부만 검사
            return false;
        }
    }

    /**
     * 토큰으로부터 Authentication 객체를 생성
     * @param token
     * @return
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
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Number authIdNumber = (Number) claims.get(AUTH_ID);
        KakaoMemberDetails principal = new KakaoMemberDetails(
                authIdNumber.longValue(),
                (String) claims.get(AUTH_EMAIL),
                simpleGrantedAuthorities,
                Map.of()
        );

        return new UsernamePasswordAuthenticationToken(principal, token, simpleGrantedAuthorities);
    }
}