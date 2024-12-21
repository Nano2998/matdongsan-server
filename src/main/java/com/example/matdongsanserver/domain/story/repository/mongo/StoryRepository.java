package com.example.matdongsanserver.domain.story.repository.mongo;

import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    default Story findByIdOrThrow(String storyId) {
        return findById(storyId).orElseThrow(
                () -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)
        );
    }
    long countByMemberId(Long memberId);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByIdIn(List<String> storyIds, Pageable pageable);
    List<Story> findByIdIn(List<String> storyIds);

    Page<Story> findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByLikesDesc(int startAge, int endAge, List<Language> languages, Pageable pageable);
    Page<Story> findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByCreatedAtDesc(int startAge, int endAge, List<Language> languages, Pageable pageable);

    /**
     * 태그 검색을 통해서 일치하는 동화를 리턴
     * @param tags
     * @param pageable
     * @return
     */
    @Query("{ 'isPublic': true, 'tags': { $regex: ?0, $options: 'i' } }")
    Page<Story> findByTags(List<String> tags, Pageable pageable);

    /**
     * 특정 작가의 모든 동화의 좋아요 수를 리턴
     * @param memberId
     * @return
     */
    @Aggregation(pipeline = {
            "{ '$match': { 'memberId': ?0 } }",
            "{ '$group': { '_id': null, 'totalLikes': { '$sum': '$likes' } } }"
    })
    Long sumLikesByMemberId(Long memberId);
}