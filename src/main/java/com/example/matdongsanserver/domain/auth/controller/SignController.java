package com.example.matdongsanserver.domain.auth.controller;

import com.example.matdongsanserver.domain.auth.jwt.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sign")
public class SignController {

    @GetMapping("/login/kakao")
    public ResponseEntity loginKakao(@RequestParam(name = "accessToken") String accessToken,
                                     @RequestParam(name = "refreshToken") String refreshToken) {
        return new ResponseEntity(TokenDto.of(accessToken, refreshToken), HttpStatus.OK);
    }
}
