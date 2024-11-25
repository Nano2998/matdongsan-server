package com.example.matdongsanserver.domain.auth.filter;

import com.example.matdongsanserver.domain.auth.jwt.TokenDto;
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
    private static final String EXCEPTION_ACCESS_HANDLER = "/api/exception/refresh-token-expired";

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

        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
            if (tokenProvider.validateExpire(accessToken)) {
                SecurityContextHolder.getContext()
                        .setAuthentication(tokenProvider.getAuthentication(accessToken));
            } else {
                String refreshToken = getTokenFromHeader(request, REFRESH_HEADER);
                if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                    if (tokenProvider.validateExpire(refreshToken)) {
                        TokenDto tokenDto = tokenProvider.reIssueAccessToken(refreshToken);
                        SecurityContextHolder.getContext()
                                .setAuthentication(tokenProvider.getAuthentication(tokenDto.getAccessToken()));

                        redirectReissueURI(request, response, tokenDto);
                        return;
                    } else {
                        //리프레시 토큰 만료
                        response.sendRedirect(EXCEPTION_ACCESS_HANDLER);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isRequestPassURI(HttpServletRequest request) {
        return request.getRequestURI().equals("/") ||
                request.getRequestURI().startsWith("/api/auth") ||
                request.getRequestURI().startsWith("/api/exception") ||
                request.getRequestURI().startsWith("/favicon.ico");
    }

    private String getTokenFromHeader(HttpServletRequest request, String header) {
        String token = request.getHeader(header);
        if (StringUtils.hasText(token)) {
            log.info("{}", token);
            return token;
        }
        return null;
    }

    private static void redirectReissueURI(HttpServletRequest request, HttpServletResponse response, TokenDto tokenDto)
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("accessToken", tokenDto.getAccessToken());
        session.setAttribute("refreshToken", tokenDto.getRefreshToken());
        response.sendRedirect("/api/auth/reissue");
    }
}