package com.example.matdongsanserver.domain.dashboard.controller;

import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.dashboard.dto.DashboardDto;
import com.example.matdongsanserver.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dashboard API", description = "대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService parentService;

    @Operation(summary = "동화 질문 생성 및 반환")
    @GetMapping("/questions/{storyId}")
    public ResponseEntity<DashboardDto.StoryQuestionResponse> registerQuestions(
            @PathVariable String storyId
    ) {
        return ResponseEntity.ok()
                .body(parentService.registerQuestions(storyId));
    }

    @Operation(summary = "대시보드 QnA 모두보기")
    @GetMapping
    public ResponseEntity<Page<DashboardDto.ParentQnaLogRequest>> getAllQna(
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(parentService.getQnaLog(SecurityUtils.getLoggedInMemberId(), pageable));
    }

    @Operation(summary = "특정 자녀 QnA 보기")
    @GetMapping("/{childId}")
    public ResponseEntity<Page<DashboardDto.ParentQnaLogRequest>> getChildQna(
            @PathVariable Long childId,
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(parentService.getChildQnaLog(childId, pageable));
    }

    @Operation(summary = "QnA 상세보기")
    @GetMapping("/detail/{qnaId}")
    public ResponseEntity<List<DashboardDto.QnAs>> getQnaDetail(
            @PathVariable Long qnaId
    ) {
        return ResponseEntity.ok().body(parentService.getQnaDetail(qnaId));
    }
}