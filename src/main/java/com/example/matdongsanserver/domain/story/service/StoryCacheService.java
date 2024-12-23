package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryCacheService {

    private final StoryRepository storyRepository;

    public Story getStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId);
    }

    public Story updateStory(String storyId, StoryDto.StoryUpdateRequest requestDto) {
        return storyRepository.findByIdOrThrow(storyId)
                .updateStoryDetail(requestDto);
    }

    public Story likeStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId)
                .addLikes();
    }

    public Story unlikeStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId)
                .removeLikes();
    }
}
