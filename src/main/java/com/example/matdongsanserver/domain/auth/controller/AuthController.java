package com.example.matdongsanserver.domain.auth.controller;

import com.example.matdongsanserver.domain.auth.dto.LoginResponse;
import com.example.matdongsanserver.domain.auth.dto.TokenResponse;
import com.example.matdongsanserver.domain.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @PostMapping("/login/kakao/code")
    public ResponseEntity<LoginResponse> loginKakao(@RequestParam(name = "code") String code)
            throws JsonProcessingException {
        return ResponseEntity.ok()
                .body(authService.kakaoLogin(code));
    }

    /**
     * 액세스 토큰 재발급 API
     */
    @GetMapping("/reissue")
    public ResponseEntity reissueToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
        String refreshToken = (String) session.getAttribute("refreshToken");

        return new ResponseEntity(new TokenResponse(accessToken, refreshToken), HttpStatus.OK);
    }
}
