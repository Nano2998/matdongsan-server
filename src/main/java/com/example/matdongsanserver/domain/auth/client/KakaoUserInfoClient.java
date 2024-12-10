package com.example.matdongsanserver.domain.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoUserInfoClient", url = "https://kapi.kakao.com")
public interface KakaoUserInfoClient {

    @PostMapping(value = "/v2/user/me", consumes = "application/x-www-form-urlencoded")
    ResponseEntity<String> getUserInfo(@RequestHeader("Authorization") String authorization);
}
