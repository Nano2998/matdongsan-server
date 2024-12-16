package com.example.matdongsanserver.domain.module.controller;

import com.example.matdongsanserver.domain.module.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Module API", description = "모듈 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "모듈로 동화 TTS 실행")
    @GetMapping("/stories/{storyId}")
    public ResponseEntity<Void> sendCommand(
            @PathVariable String storyId
    ) {
        moduleService.sendStory(storyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모듈로 동화 질문 TTS 전송 및 응답")
    @GetMapping("/questions/{storyQuestionId}/{childId}")
    public ResponseEntity<Void> generateQuestions(
            @PathVariable Long storyQuestionId,
            @PathVariable Long childId
    ) {
        moduleService.sendQuestion(storyQuestionId, childId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모듈로 아이의 응답을 받아서 STT로 전송 및 저장")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAnswer(
            @RequestPart("file")MultipartFile file
    ) {
        moduleService.uploadAnswer(file);
        return ResponseEntity.noContent().build();
    }
}