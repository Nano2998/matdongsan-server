package com.example.matdongsanserver.domain.auth.controller;

import com.example.matdongsanserver.domain.auth.dto.LoginRequest;
import com.example.matdongsanserver.domain.auth.dto.LoginResponse;
import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
import com.example.matdongsanserver.domain.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "인증 코드를 통해 카카오 토큰 반환 (테스트용)")
    @GetMapping("/kakao/token")
    public ResponseEntity<String> getKakaoToken(@RequestParam(name = "code") String code)
            throws JsonProcessingException {
        return ResponseEntity.ok()
                .body(authService.getToken(code));
    }

    @Operation(summary = "카카오 토큰과 이메일을 통해서 로그인 수행 (처음 로그인일 경우 회원을 생성)")
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponse> loginKakao(@RequestBody LoginRequest loginRequest)
            throws JsonProcessingException {
        return ResponseEntity.ok()
                .body(authService.kakaoLogin(loginRequest));
    }

    @Operation(summary = "리프레쉬 토큰을 통해 에세스 토큰 및 리프레쉬 토큰 재발급")
    @GetMapping("/reissue")
    public ResponseEntity<TokenResponse> reissueToken(HttpServletRequest request) {
        return ResponseEntity.ok()
                .body(authService.reissueAccessToken(request));
    }
}
