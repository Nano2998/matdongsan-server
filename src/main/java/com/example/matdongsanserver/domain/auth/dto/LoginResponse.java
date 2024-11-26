package com.example.matdongsanserver.domain.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse{

    private String accessToken;
    private String refreshToken;
    private boolean isFirstLogin;

    @Builder
    public LoginResponse(String accessToken, String refreshToken, boolean isFirstLogin) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isFirstLogin = isFirstLogin;
    }
}
