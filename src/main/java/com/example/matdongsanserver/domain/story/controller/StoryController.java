package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    @PostMapping
    public ResponseEntity<StoryDto.StoryCreationResponse> generateStory(@RequestBody StoryDto.StoryCreationRequest requestDto) throws IOException {
        return ResponseEntity.ok()
                .body(storyService.generateStory(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<StoryDto.StorySummary>> getAllStories() {
        return ResponseEntity.ok()
                .body(storyService.getAllStories());
    }

    @PatchMapping("/{storyId}")
    public ResponseEntity<Void> updateStoryDetail(@PathVariable String storyId, @RequestBody StoryDto.StoryUpdateRequest requestDto) {
        storyService.updateStoryDetail(storyId, requestDto);
        return ResponseEntity.ok().build();
    }
}
