package com.example.matdongsanserver.domain.story.repository.mongo;

import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    long countByMemberId(Long memberId);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByIdIn(List<String> storyIds, Pageable pageable);
    List<Story> findByIdIn(List<String> storyIds);

    Page<Story> findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByLikesDesc(int startAge, int endAge, List<Language> languages, Pageable pageable);
    Page<Story> findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByCreatedAtDesc(int startAge, int endAge, List<Language> languages, Pageable pageable);

    @Query("{ 'isPublic': true, 'tags': { $regex: ?0, $options: 'i' } }")
    Page<Story> findByTags(List<String> tags, Pageable pageable);
}