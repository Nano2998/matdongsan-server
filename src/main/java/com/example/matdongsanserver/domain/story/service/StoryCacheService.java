package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 동화 캐시를 위한 서비스
 * storyId를 key로 캐시
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoryCacheService {

    private final StoryRepository storyRepository;

    @Cacheable(value = "stories", key = "#storyId", cacheManager = "customCacheManager")
    public Story getStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId);
    }

    @CachePut(value = "stories", key = "#storyId", cacheManager = "customCacheManager")
    public Story updateStory(String storyId, StoryDto.StoryUpdateRequest requestDto) {
        return storyRepository.findByIdOrThrow(storyId)
                .updateStoryDetail(requestDto);
    }

    @CachePut(value = "stories", key = "#storyId", cacheManager = "customCacheManager")
    public Story likeStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId)
                .addLikes();
    }

    @CachePut(value = "stories", key = "#storyId", cacheManager = "customCacheManager")
    public Story unlikeStory(String storyId) {
        return storyRepository.findByIdOrThrow(storyId)
                .removeLikes();
    }
}
