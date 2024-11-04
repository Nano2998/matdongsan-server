package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Story API", description = "동화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "동화 생성")
    @PostMapping("/{memberId}")
    public ResponseEntity<StoryDto.StoryCreationResponse> generateStory(
            @RequestBody StoryDto.StoryCreationRequest requestDto,
            @PathVariable Long memberId
    ) throws IOException {
        return ResponseEntity.ok()
                .body(storyService.generateStory(memberId, requestDto));
    }

    @Operation(summary = "전체 동화 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getAllStories(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getAllStories());
    }

    @Operation(summary = "동화 상세 수정")
    @PatchMapping("/{memberId}/{storyId}")
    public ResponseEntity<StoryDto.StoryDetail> updateStoryDetail(
            @PathVariable Long memberId,
            @PathVariable String storyId,
            @RequestBody StoryDto.StoryUpdateRequest requestDto
    ) {
        return ResponseEntity.ok()
                .body(storyService.updateStoryDetail(memberId, storyId, requestDto));
    }

    @Operation(summary = "동화 상세 조회")
    @GetMapping("/{memberId}/{storyId}")
    public ResponseEntity<StoryDto.StoryDetail> getStoryDetail(
            @PathVariable Long memberId,
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getStoryDetail(storyId));
    }

    @Operation(summary = "영어 동화 번역")
    @GetMapping("/translation/{memberId}/{storyId}")
    public ResponseEntity<StoryDto.StoryTranslationResponse> translateStory(
            @PathVariable Long memberId,
            @PathVariable String storyId
    ) throws IOException {
        return ResponseEntity.ok()
                .body(storyService.translationStory(storyId));
    }

    @Operation(summary = "동화 TTS")
    @GetMapping("/tts/{memberId}/{storyId}")
    public ResponseEntity<String> getStoryTTS(
            @PathVariable Long memberId,
            @PathVariable String storyId
    ) throws IOException {
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        responseHeaders.setContentDispositionFormData("attachment", "story_tts.mp3");
//        Resource resource = storyService.getStoryTTS(storyId);
//        return ResponseEntity.ok()
//                .headers(responseHeaders)
//                .contentLength(resource.contentLength())
//                .body(resource);
        return ResponseEntity.ok()
                .body(storyService.getStoryTTS(storyId));
    }
}
