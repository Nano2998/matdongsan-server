package com.example.matdongsanserver.common.utils;

import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.exception.AuthException;
import com.example.matdongsanserver.domain.auth.kakao.KakaoMemberDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 현재 로그인한 멤버의 아이디를 반환
     * @return memberId
     */
    public static Long getLoggedInMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof KakaoMemberDetails principal)) {
            throw new AuthException(AuthErrorCode.LOGIN_REQUIRED);
        }
        return principal.getId();
    }
}
