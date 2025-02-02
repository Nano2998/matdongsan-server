package com.example.matdongsanserver.domain.auth.handler;

import com.example.matdongsanserver.common.exception.ErrorResponse;
import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증 없이 접근을 시도하는 경우
@Slf4j
@Component
public class JwtAuthenticationFailEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String clientIP = request.getRemoteAddr();

        log.warn("[맛동산]: 예외 발생, 예외내용 = Authentication failed. Method: {}, URI: {}, Client IP: {}", method, requestURI, clientIP);

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.LOGIN_REQUIRED);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}
