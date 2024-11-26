package com.example.matdongsanserver.domain.auth.filter;

import com.example.matdongsanserver.common.exception.ErrorResponse;
import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String ACCESS_HEADER = "AccessToken";
    private static final String REFRESH_HEADER = "RefreshToken";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {

        String accessToken = getTokenFromHeader(request, ACCESS_HEADER);

        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
            if (tokenProvider.validateExpire(accessToken)) {
                SecurityContextHolder.getContext()
                        .setAuthentication(tokenProvider.getAuthentication(accessToken));
            } else {
                String refreshToken = getTokenFromHeader(request, REFRESH_HEADER);
                if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                    if (tokenProvider.validateExpire(refreshToken)) {
                        TokenResponse tokenResponse = tokenProvider.reissueAccessToken(refreshToken);
                        SecurityContextHolder.getContext()
                                .setAuthentication(tokenProvider.getAuthentication(tokenResponse.getAccessToken()));

                        redirectReissueURI(request, response, tokenResponse);
                        return;
                    } else {
                        //리프레시 토큰 만료
                        response.setContentType("application/json; charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.REFRESH_TOKEN_EXPIRED);

                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

                        response.getWriter().write(jsonResponse);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request, String header) {
        String token = request.getHeader(header);
        if (StringUtils.hasText(token)) {
            log.info("{}", token);
            return token;
        }
        return null;
    }

    private static void redirectReissueURI(HttpServletRequest request, HttpServletResponse response, TokenResponse tokenResponse)
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("accessToken", tokenResponse.getAccessToken());
        session.setAttribute("refreshToken", tokenResponse.getRefreshToken());
        response.sendRedirect("/api/auth/reissue");
    }
}