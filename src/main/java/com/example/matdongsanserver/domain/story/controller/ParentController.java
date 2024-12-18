package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.story.dto.ParentDto;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.service.ParentService;
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
public class ParentController {

    private final ParentService parentService;

    @Operation(summary = "대시보드 QnA 모두보기")
    @GetMapping
    public ResponseEntity<Page<ParentDto.ParentQnaLogRequest>> getAllQna(
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(parentService.getQnaLog(SecurityUtils.getLoggedInMemberId(), pageable));
    }

    @Operation(summary = "특정 자녀 QnA 보기")
    @GetMapping("/{childId}")
    public ResponseEntity<Page<ParentDto.ParentQnaLogRequest>> getChildQna(
            @PathVariable Long childId,
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(parentService.getChildQnaLog(childId, pageable));
    }

    @Operation(summary = "QnA 상세보기")
    @GetMapping("/detail/{qnaId}")
    public ResponseEntity<List<StoryDto.QnAs>> getQnaDetail(
            @PathVariable Long qnaId
    ) {
        return ResponseEntity.ok().body(parentService.getQnaDetail(qnaId));
    }
}