package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.common.utils.SecurityUtils;
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
    @PostMapping
    public ResponseEntity<StoryDto.StoryCreationResponse> registerStory(
            @RequestBody StoryDto.StoryCreationRequest requestDto
    ) {
        return ResponseEntity.ok()
                .body(storyService.registerStory(SecurityUtils.getLoggedInMemberId(), requestDto));
    }

    @Operation(summary = "동화 상세 수정")
    @PatchMapping("/{storyId}")
    public ResponseEntity<StoryDto.StoryDetail> updateStoryDetail(
            @PathVariable String storyId,
            @RequestBody StoryDto.StoryUpdateRequest requestDto
    ) {
        return ResponseEntity.ok()
                .body(storyService.updateStoryDetail(SecurityUtils.getLoggedInMemberId(), storyId, requestDto));
    }

    @Operation(summary = "동화 상세 조회")
    @GetMapping("/{storyId}")
    public ResponseEntity<StoryDto.StoryDetail> getStoryDetail(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getStoryDetail(storyId,SecurityUtils.getLoggedInMemberId()));
    }

    @Operation(summary = "동화 TTS")
    @GetMapping("/tts/{storyId}")
    public ResponseEntity<StoryDto.TTSResponse> getStoryTTS(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getOrRegisterStoryTTS(storyId));
    }

    @Operation(summary = "동화 좋아요")
    @PostMapping("/likes/{storyId}")
    public ResponseEntity<Void> likeStory(
            @PathVariable String storyId
    ) {
        storyService.likeStory(storyId, SecurityUtils.getLoggedInMemberId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "동화 좋아요 취소")
    @DeleteMapping("/likes/{storyId}")
    public ResponseEntity<Void> unlikeStory(
            @PathVariable String storyId
    ) {
        storyService.unlikeStory(storyId, SecurityUtils.getLoggedInMemberId());
        return ResponseEntity.noContent().build();
    }
}