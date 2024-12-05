package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.auth.util.SecurityUtils;
import com.example.matdongsanserver.domain.story.AgeType;
import com.example.matdongsanserver.domain.story.LangType;
import com.example.matdongsanserver.domain.story.SortType;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Library API", description = "라이브러리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(summary = "조건별 동화 리스트 조회")
    @GetMapping
    public ResponseEntity<Page<StoryDto.StorySummary>> getStories(
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "all") String language,
            @RequestParam(defaultValue = "main") String age,
            Pageable pageable
    ) {
        SortType sortType = SortType.fromString(sort);
        LangType langType = LangType.fromString(language);
        AgeType ageType = AgeType.fromString(age);
        return ResponseEntity.ok(libraryService.getStories(ageType,langType, sortType, pageable));
    }

    @Operation(summary = "특정 작가의 동화 리스트 조회")
    @GetMapping("/{authorId}")
    public ResponseEntity<Page<StoryDto.StorySummary>> getStoriesByWriter(
            @RequestParam(defaultValue = "recent") String sort,
            @PathVariable Long authorId,
            Pageable pageable
    ) {
        SortType sortType = SortType.fromString(sort);
        return ResponseEntity.ok(libraryService.getStoriesByAuthorId(authorId, sortType, pageable));
    }

    @Operation(summary = "내 동화 리스트 조회")
    @GetMapping("/my")
    public ResponseEntity<Page<StoryDto.StorySummary>> getMyStories(
            @RequestParam(defaultValue = "recent") String sort,
            Pageable pageable
    ) {
        SortType sortType = SortType.fromString(sort);
        return ResponseEntity.ok(libraryService.getMyStories(SecurityUtils.getLoggedInMemberId(), sortType, pageable));
    }

    @Operation(summary = "좋아요 누른 동화 리스트 조회")
    @GetMapping("/likes")
    public ResponseEntity<Page<StoryDto.StorySummary>> getLikedStories(
            Pageable pageable
    ) {
        return ResponseEntity.ok()
                .body(libraryService.getLikedStories(SecurityUtils.getLoggedInMemberId(), pageable));
    }

    @Operation(summary = "최근 본 동화 리스트 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<StoryDto.StorySummary>> getRecentStories() {
        return ResponseEntity.ok().body(libraryService.getRecentStories(SecurityUtils.getLoggedInMemberId()));
    }
}
