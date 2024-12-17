package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.ParentDto;
import com.example.matdongsanserver.domain.story.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard API", description = "대시보드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class ParentController {

    private final ParentService parentService;

    @Operation(summary = "대시보드 QnA 모두보기")
    @GetMapping
    public ResponseEntity<Page<ParentDto.ParentQnaLogRequest>> getChildQna(
            @RequestParam Long memberId,
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(parentService.getQnaLog(memberId, pageable));
    }
}
