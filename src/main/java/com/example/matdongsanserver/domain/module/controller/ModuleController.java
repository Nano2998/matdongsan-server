package com.example.matdongsanserver.domain.module.controller;

import com.example.matdongsanserver.domain.module.service.ModuleService;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "동화 질문 생성 및 모듈로 TTS 전송")
    @GetMapping("/questions/{storyId}/{childId}")
    public ResponseEntity<StoryDto.StoryQuestionResponse> generateQuestions(
            @PathVariable String storyId,
            @PathVariable Long childId
    ) {
        return ResponseEntity.ok()
                .body(moduleService.sendQuestion(storyId, childId));
    }
}
