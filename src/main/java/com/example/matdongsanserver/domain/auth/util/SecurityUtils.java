package com.example.matdongsanserver.domain.auth.util;

import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.exception.AuthException;
import com.example.matdongsanserver.domain.auth.kakao.KakaoMemberDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getLoggedInMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof KakaoMemberDetails principal)) {
            throw new AuthException(AuthErrorCode.LOGIN_REQUIRED);
        }
        return principal.getId();
    }
}
