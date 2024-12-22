package com.example.matdongsanserver.domain.auth.filter;

import com.example.matdongsanserver.common.exception.ErrorResponse;
import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String ACCESS_HEADER = "accessToken";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {

        // swagger 및 헬스 체크 api는 열어둠
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth") || requestURI.startsWith("/api/swagger") ||
                requestURI.startsWith("/api/health") || requestURI.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getTokenFromHeader(request, ACCESS_HEADER);

        if (StringUtils.hasText(accessToken)) {
            if (tokenProvider.validateToken(accessToken)) {
                if (tokenProvider.validateTokenExpired(accessToken)) {
                    SecurityContextHolder.getContext().setAuthentication(tokenProvider.getAuthentication(accessToken));
                } else {
                    handleAccessTokenExpired(request, response);
                    return;
                }
            } else {
                handleInvalidToken(request, response);
                return;
            }
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request, String header) {
        String token = request.getHeader(header);
        if (StringUtils.hasText(token)) {
            return token;
        }
        return null;
    }

    /**
     * 에세스 토큰 만료시에 401 응답 반환
     */
    private void handleAccessTokenExpired(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logWarning(request, "Access token expired.");

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.ACCESS_TOKEN_EXPIRED);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 잘못된 형식의 토큰일 때
     */
    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logWarning(request, "Invalid access token.");

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.INVALID_TOKEN);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 경고 로그를 기록하는 공통 메서드
     */
    private void logWarning(HttpServletRequest request, String message) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String clientIP = request.getRemoteAddr();

        log.warn("[맛동산]: 예외 발생, 예외내용 = {} Method: {}, URI: {}, Client IP: {}", message, method, requestURI, clientIP);
    }
}