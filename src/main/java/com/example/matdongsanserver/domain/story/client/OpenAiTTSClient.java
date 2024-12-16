package com.example.matdongsanserver.domain.story.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "openAiTTSClient", url = "${openai.tts.url}")
public interface OpenAiTTSClient {

    /**
     * TTS 생성용
     * @param authorization
     * @param contentType
     * @param requestBody
     * @return
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<byte[]> sendTTSRequest(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody Map<String, Object> requestBody
    );
}
