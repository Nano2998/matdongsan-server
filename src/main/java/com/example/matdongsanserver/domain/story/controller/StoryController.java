package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.request.StoryCreationRequest;
import com.example.matdongsanserver.domain.story.dto.request.StoryUpdateRequest;
import com.example.matdongsanserver.domain.story.dto.response.StoryCreationResponse;
import com.example.matdongsanserver.domain.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    public ResponseEntity<StoryCreationResponse> generateStory(@RequestBody StoryCreationRequest requestDto) throws IOException {
        return ResponseEntity.ok()
                .body(storyService.generateStory(requestDto));
    }

    @PatchMapping("/{storyId}")
    public ResponseEntity<Void> updateStoryDetail(@PathVariable String storyId, @RequestBody StoryUpdateRequest requestDto) {
        storyService.updateStoryDetail(storyId, requestDto);
        return ResponseEntity.ok().build();
    }
}
