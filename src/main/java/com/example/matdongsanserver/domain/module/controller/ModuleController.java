package com.example.matdongsanserver.domain.module.controller;

import com.example.matdongsanserver.domain.module.service.ModuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Module API", description = "모듈 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping("/send/{storyId}")
    public ResponseEntity<Void> sendCommand(
            @PathVariable String storyId
    ) throws IOException {
        moduleService.sendCommand(storyId);
        return ResponseEntity.noContent().build();
    }
}
