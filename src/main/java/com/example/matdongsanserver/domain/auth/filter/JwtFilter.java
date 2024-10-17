package com.example.matdongsanserver.domain.auth.filter;

import com.example.matdongsanserver.domain.auth.dto.TokenDto;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
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
        if (isRequestPassURI(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getTokenFromHeader(request, ACCESS_HEADER);

        if (tokenProvider.validateExpire(accessToken) && tokenProvider.validateToken(accessToken)) {
            String refreshToken = getTokenFromHeader(request, REFRESH_HEADER);
            if (tokenProvider.validateExpire(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                // accessToken, refreshToken 재발급
                TokenDto tokenDto = tokenProvider.reIssueAccessToken(refreshToken);
                SecurityContextHolder.getContext()
                        .setAuthentication(tokenProvider.getAuthentication(tokenDto.getAccessToken()));

                redirectReissueURI(request, response, tokenDto);
            }
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isRequestPassURI(HttpServletRequest request) {
        if (request.getRequestURI().equals("/")) {
            return true;
        }

        if (request.getRequestURI().startsWith("/api/auth")) {
            return true;
        }

        if (request.getRequestURI().startsWith("/api/exception")) {
            return true;
        }

        if (request.getRequestURI().startsWith("/favicon.ico")) {
            return true;
        }

        return false;
    }

    private String getTokenFromHeader(HttpServletRequest request, String header) {
        String token = request.getHeader(header);
        if (StringUtils.hasText(token)) {
            return token;
        }
        return null;
    }

    private static void redirectReissueURI(HttpServletRequest request, HttpServletResponse response, TokenDto tokenDto)
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("accessToken", tokenDto.getAccessToken());
        session.setAttribute("refreshToken", tokenDto.getRefreshToken());
        response.sendRedirect("/api/sign/reissue");
    }
}
