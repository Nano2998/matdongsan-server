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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;

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
    public Page<StoryDto.StorySummary> getStoriesByMemberId(Long authorId, SortType sortType, Pageable pageable) {
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
}