package com.example.matdongsanserver.domain.auth.dto;

import lombok.Getter;

@Getter
public class LoginResponse extends TokenResponse {

    private final boolean hasNoChildren;

    private LoginResponse(final String accessToken, final String refreshToken, boolean hasNoChildren) {
        super(accessToken, refreshToken);
        this.hasNoChildren = hasNoChildren;
    }

    public static LoginResponse of(final String accessToken, final String refreshToken, final boolean hasNoChildren) {
        return new LoginResponse(accessToken, refreshToken, hasNoChildren);
    }
}
