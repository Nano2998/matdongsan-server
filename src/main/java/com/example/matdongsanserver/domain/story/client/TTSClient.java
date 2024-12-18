package com.example.matdongsanserver.domain.story.client;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "TTSClient", url = "${tts.url}")
public interface TTSClient {

    /**
     * TTS 생성용
     * @param contentType
     * @param ttsCreationRequest
     * @return
     */
    @PostMapping(value = "/generate-tts", consumes = "application/json", produces = "application/json")
    ResponseEntity<byte[]> sendTTSRequest(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody StoryDto.TTSCreationRequest ttsCreationRequest
    );
}
