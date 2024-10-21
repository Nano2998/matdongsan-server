package com.example.matdongsanserver.domain.auth.kakao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class KakaoUserInfo {

    public static final String KAKAO_ACCOUNT = "kakao_account";
    public static final String EMAIL = "email";
    public static final String NICKNAME = "nickname";
    public static final String PROFILE = "profile";
    public static final String PROFILE_IMAGE = "profile_image_url";

    private Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    private Map<String, Object> getKakaoAccount() {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        Object kakaoAccount = attributes.get(KAKAO_ACCOUNT);
        return objectMapper.convertValue(kakaoAccount, typeRef);
    }

    private Map<String, Object> getProfile() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        Object profile = kakaoAccount.get(PROFILE);
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        return objectMapper.convertValue(profile, typeRef);
    }

    public String getEmail() {
        Map<String, Object> account = getKakaoAccount();
        return account.get(EMAIL).toString();
    }

    public String getNickname() {
        Map<String, Object> profileMap = getProfile();
        return profileMap.get(NICKNAME).toString();
    }

    public String getProfileImage() {
        Map<String, Object> profileMap = getProfile();
        return profileMap.get(PROFILE_IMAGE).toString();
    }
}