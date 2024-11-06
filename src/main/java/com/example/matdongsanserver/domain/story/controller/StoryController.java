package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ) {
        return ResponseEntity.ok()
                .body(storyService.generateStory(memberId, requestDto));
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
    @GetMapping("/{storyId}")
    public ResponseEntity<StoryDto.StoryDetail> getStoryDetail(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getStoryDetail(storyId));
    }

    @Operation(summary = "영어 동화 번역")
    @GetMapping("/translation/{storyId}")
    public ResponseEntity<StoryDto.StoryTranslationResponse> translateStory(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.translationStory(storyId));
    }

    @Operation(summary = "동화 TTS")
    @GetMapping("/tts/{storyId}")
    public ResponseEntity<String> getStoryTTS(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getStoryTTS(storyId));
    }

    @Operation(summary = "동화 좋아요")
    @PostMapping("/likes/{memberId}/{storyId}")
    public ResponseEntity<Void> likeStory(
            @PathVariable Long memberId,
            @PathVariable String storyId
    ) {
        storyService.addLike(storyId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "동화 좋아요 취소")
    @DeleteMapping("/likes/{memberId}/{storyId}")
    public ResponseEntity<Void> unlikeStory(
            @PathVariable Long memberId,
            @PathVariable String storyId
    ) {
        storyService.removeLike(storyId, memberId);
        return ResponseEntity.noContent().build();
    }
}
