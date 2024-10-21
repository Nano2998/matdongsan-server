package com.example.matdongsanserver.domain.auth.controller;

import com.example.matdongsanserver.domain.auth.exception.AuthErrorCode;
import com.example.matdongsanserver.domain.auth.exception.AuthException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exception")
public class AuthExceptionController {

    @GetMapping("/access-denied")
    public void accessDeniedException() {
        throw new AuthException(AuthErrorCode.ACCESS_DENIED);
    }

    @GetMapping("/entry-point")
    public void authenticateException() {
        throw new AuthException(AuthErrorCode.LOGIN_REQUIRED);
    }

    @GetMapping("/refresh-token-expired")
    public void refreshTokenExpiredException() {
        throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
