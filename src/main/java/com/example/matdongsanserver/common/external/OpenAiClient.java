package com.example.matdongsanserver.common.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "openAiClient", url = "${openai.api.url}")
public interface OpenAiClient {

    /**
     * 일반 요청용
     * @param authorization
     * @param contentType
     * @param requestBody
     * @return
     */
    @PostMapping(value = "/chat/completions", consumes = "application/json", produces = "application/json")
    ResponseEntity<String> sendChatRequest(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody Map<String, Object> requestBody
    );

    /**
     * 이미지 생성용
     * @param authorization
     * @param contentType
     * @param requestBody
     * @return
     */
    @PostMapping(value = "/images/generations", consumes = "application/json", produces = "application/json")
    ResponseEntity<String> sendImageRequest(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody Map<String, Object> requestBody
    );

    /**
     * STT 생성용
     * @param authorization
     * @param file
     * @return
     */
    @PostMapping(value = "/audio/transcriptions", consumes = "multipart/form-data")
    ResponseEntity<String> sendSTTRequest(
            @RequestHeader("Authorization") String authorization,
            @RequestPart("model") String model,
            @RequestPart("file") MultipartFile file
    );
}
