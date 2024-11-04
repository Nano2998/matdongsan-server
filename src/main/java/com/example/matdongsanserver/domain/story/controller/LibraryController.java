package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Library API", description = "라이브러리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryController {

    private final StoryService storyService;

    @Operation(summary = "전체 동화 조회 - 최신순")
    @GetMapping("/recent")
    public ResponseEntity<List<StoryDto.StorySummary>> getRecentStories(
    ) {
        return ResponseEntity.ok()
                .body(storyService.getRecentStories());
    }

    @Operation(summary = "전체 동화 조회 - 좋아요순")
    @GetMapping("/popular")
    public ResponseEntity<List<StoryDto.StorySummary>> getPopularStories(
    ) {
        return ResponseEntity.ok()
                .body(storyService.getPopularStories());
    }

    @Operation(summary = "특정 작가의 동화 조회 - 최신순")
    @GetMapping("/recent/{writerId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getRecentStoriesByWriter(
            @PathVariable Long writerId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getRecentStoriesByMemberId(writerId));
    }

    @Operation(summary = "특정 작가의 동화 조회 - 좋아요순")
    @GetMapping("/popular/{writerId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getPopularStoriesByWriter(
            @PathVariable Long writerId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getPopularStoriesByMemberId(writerId));
    }

    @Operation(summary = "좋아요 누른 동화 조회")
    @GetMapping("/likes/{memberId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getLikedStories(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getLikedStories(memberId));
    }

    @Operation(summary = "내 동화 조회 - 최신순")
    @GetMapping("/recent/my/{memberId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getRecentMyStories(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getRecentMyStories(memberId));
    }

    @Operation(summary = "내 동화 조회 - 좋아요순")
    @GetMapping("/popular/my/{memberId}")
    public ResponseEntity<List<StoryDto.StorySummary>> getPopularMyStories(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(storyService.getPopularMyStories(memberId));
    }
}
