package com.example.matdongsanserver.domain.auth.filter;

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

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String ACCESS_HEADER = "AccessToken";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {
        if (isRequestPassURI(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getTokenFromHeader(request);

        /*
        if (!tokenProvider.validateExpire(accessToken) && tokenProvider.validateToken(accessToken)) {
            String redirectUrl =
                    "http://" + request.getServerName() + "/api/exception/access-token-expired";
            response.sendRedirect(redirectUrl);
            return;
        }
         */

        if (tokenProvider.validateExpire(accessToken) && tokenProvider.validateToken(accessToken)) {
            SecurityContextHolder.getContext().setAuthentication(tokenProvider.getAuthentication(accessToken));
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

    private String getTokenFromHeader(HttpServletRequest request) {
        String token = request.getHeader(JwtFilter.ACCESS_HEADER);
        if (StringUtils.hasText(token)) {
            return token;
        }
        return null;
    }
}
