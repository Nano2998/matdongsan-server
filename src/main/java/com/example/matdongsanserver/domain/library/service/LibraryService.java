package com.example.matdongsanserver.domain.library.service;

import com.example.matdongsanserver.domain.library.AgeType;
import com.example.matdongsanserver.domain.library.LangType;
import com.example.matdongsanserver.domain.library.SortType;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    private static final int MAX_RECENT_TALES = 50;
    private static final int TTL_DAYS = 5;

    /**
     * 조건별 동화 리스트 조회
     * @param ageType
     * @param langType
     * @param sortType
     * @param pageable
     * @return
     */
    public Page<StoryDto.StorySummary> getStories(AgeType ageType, LangType langType, SortType sortType, Pageable pageable) {
        List<Language> languages = switch (langType) {
            case KO -> List.of(Language.KO);
            case EN -> List.of(Language.EN);
            case ALL -> List.of(Language.KO,Language.EN); // 기본값
        };

        int startAge = ageType.getStartAge() - 1; // between을 위한 값 수정
        int endAge = ageType.getEndAge() + 1; // between을 위한 값 수정

        return switch (sortType) {
            case POPULAR -> storyRepository.findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByLikesDesc(startAge, endAge,languages, pageable)
                    .map(StoryDto.StorySummary::new);
            case RECENT -> storyRepository.findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByCreatedAtDesc(startAge, endAge,languages, pageable)
                    .map(StoryDto.StorySummary::new);
        };
    }

    /**
     * 특정 작가의 동화 리스트 조회
     * @param authorId
     * @param sortType
     * @param pageable
     * @return
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
     * @param memberId
     * @param sortType
     * @param pageable
     * @return
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
     * @param memberId
     * @param pageable
     * @return
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

        redisTemplate.opsForList().remove(redisKey, 0, storyId);  // 중복 제거
        redisTemplate.opsForList().leftPush(redisKey, storyId);  // 리스트에 추가
        redisTemplate.opsForList().trim(redisKey, 0, MAX_RECENT_TALES - 1);  // 리스트 길이 제한
        redisTemplate.expire(redisKey, TTL_DAYS, TimeUnit.DAYS);  // TTL 설정
    }


    /**
     * 최근 본 동화 리스트
     * @param memberId
     * @return
     */
    public Page<StoryDto.StorySummary> getRecentStories(Long memberId, Pageable pageable) {
        List<String> recentStoryIds = getRecentStoryIds(memberId, pageable);
        if (recentStoryIds == null || recentStoryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<StoryDto.StorySummary> storySummaries = storyRepository.findByIdIn(recentStoryIds)
                .stream()
                .map(StoryDto.StorySummary::new)
                .toList();

        Long total = getRecentStoriesSize(memberId);
        return new PageImpl<>(storySummaries, pageable, total != null ? total : 0);
    }

    /**
     * 최근 본 동화의 아이디 리스트를 가져옴
     * @param memberId
     * @return
     */
    private List<String> getRecentStoryIds(Long memberId, Pageable pageable) {
        String redisKey = "user:" + memberId + ":recentTales";

        // 정해진 범위의 아이디만 반환
        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;
        return redisTemplate.opsForList().range(redisKey, start, end);
    }

    /**
     * 최근 본 동화 리스트의 길이 반환
     * @param memberId
     * @return
     */
    private Long getRecentStoriesSize(Long memberId) {
        String redisKey = "user:" + memberId + ":recentTales";
        return redisTemplate.opsForList().size(redisKey);
    }

    /**
     * 동화 검색
     * @param tags
     * @param pageable
     * @return
     */
    public Page<StoryDto.StorySummary> searchStories(List<String> tags, Pageable pageable) {
        return storyRepository.findByTags(tags, pageable)
                .map(StoryDto.StorySummary::new);
    }
}