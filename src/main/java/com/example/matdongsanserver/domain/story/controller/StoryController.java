package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.request.StoryCreationRequest;
import com.example.matdongsanserver.domain.story.dto.response.StoryCreationResponse;
import com.example.matdongsanserver.domain.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    @PostMapping("")
    public StoryCreationResponse generateStory(@RequestBody StoryCreationRequest requestDto) throws IOException {
        return storyService.generateStory(requestDto);
    }
}
