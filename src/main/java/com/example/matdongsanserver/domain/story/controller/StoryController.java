package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.domain.story.dto.request.StoryRequestDto;
import com.example.matdongsanserver.domain.story.dto.response.StoryResponseDto;
import com.example.matdongsanserver.domain.story.service.StoryService;
import jakarta.annotation.Resource;
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

    @PostMapping("/generate")
    public StoryResponseDto generateStory(@RequestBody StoryRequestDto requestDto) throws IOException {
        return storyService.generateStory(requestDto);
    }
}
