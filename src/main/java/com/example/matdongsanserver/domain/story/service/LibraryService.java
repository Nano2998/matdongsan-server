package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.story.SortType;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_RECENT_TALES = 50; // 최대 리스트 길이
    private static final int TTL_DAYS = 5; // TTL 설정

    /**
     * 전체 동화 리스트 조회
     */
    public Page<StoryDto.StorySummary> getStories(SortType sortType, Pageable pageable) {
        return switch (sortType) {
            case POPULAR -> storyRepository.findByIsPublicTrueOrderByLikesDesc(pageable)
                    .map(StoryDto.StorySummary::new);
            case RECENT -> storyRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable)
                    .map(StoryDto.StorySummary::new);
        };
    }

    /**
     * 특정 작가의 동화 리스트 조회
     */
    public Page<StoryDto.StorySummary> getStoriesByAuthorId(Long authorId, SortType sortType, Pageable pageable) {
        return switch (sortType) {
            case POPULAR -> storyRepository.findByIsPublicTrueAndMemberIdOrderByLikesDesc(authorId, pageable)
                    .map(StoryDto.StorySummary::new);
            case RECENT -> storyRepository.findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(authorId, pageable)
                    .map(StoryDto.StorySummary::new);
        };
    }

    /**
     * 내가 만든 동화 리스트 조회
     */
    public Page<StoryDto.StorySummary> getMyStories(Long memberId, SortType sortType, Pageable pageable) {
        return switch (sortType) {
            case POPULAR -> storyRepository.findByMemberIdOrderByLikesDesc(memberId, pageable)
                    .map(StoryDto.StorySummary::new);
            case RECENT -> storyRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                    .map(StoryDto.StorySummary::new);
        };
    }

    /**
     * 좋아요 누른 동화 리스트
     */
    public Page<StoryDto.StorySummary> getLikedStories(Long memberId, Pageable pageable) {
        Page<StoryLike> likedStoryIdsPage = storyLikeRepository.findByMemberId(memberId, pageable);

        return storyRepository.findByIdIn(likedStoryIdsPage.getContent()
                        .stream()
                        .map(StoryLike::getStoryId)
                        .toList(), pageable)
                .map(StoryDto.StorySummary::new);
    }

    /**
     * 최근 본 동화 추가
     * @param memberId
     * @param storyId
     */
    public void addRecentStories(Long memberId, String storyId) {
        String redisKey = "user:" + memberId + ":recentTales";
        // 중복 제거
        redisTemplate.opsForList().remove(redisKey, 0, storyId);

        // 리스트에 추가
        redisTemplate.opsForList().leftPush(redisKey, storyId);

        // 리스트 길이 제한
        redisTemplate.opsForList().trim(redisKey, 0, MAX_RECENT_TALES - 1);

        // TTL 설정
        redisTemplate.expire(redisKey, TTL_DAYS, TimeUnit.DAYS);
    }


    /**
     * 최근 본 동화 리스트
     * @param memberId
     * @return
     */
    public List<StoryDto.StorySummary> getRecentStories(Long memberId) {
        List<String> recentStoryIds = getRecentStoryIds(memberId);
        return storyRepository.findByIdIn(recentStoryIds)
                .stream()
                .map(StoryDto.StorySummary::new)
                .toList();
    }

    private List<String> getRecentStoryIds(Long memberId) {
        String redisKey = "user:" + memberId + ":recentTales";
        return redisTemplate.opsForList().range(redisKey, 0, -1);
    }
}