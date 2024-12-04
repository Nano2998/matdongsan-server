package com.example.matdongsanserver.domain.story.repository.mongo;

import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    List<Story> findByIsPublicTrueOrderByCreatedAtDesc();
    List<Story> findByIsPublicTrueOrderByLikesDesc();
    List<Story> findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(Long memberId);
    List<Story> findByIsPublicTrueAndMemberIdOrderByLikesDesc(Long memberId);
    List<Story> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    List<Story> findByMemberIdOrderByLikesDesc(Long memberId);
    List<Story> findByIdIn(List<String> storyIds);
    long countByMemberId(Long memberId);

    Page<Story> findByIsPublicTrueOrderByLikesDesc(Pageable pageable);
    Page<Story> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByIsPublicTrueAndMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Story> findByMemberIdOrderByLikesDesc(Long memberId, Pageable pageable);
    Page<Story> findByIdIn(List<String> storyIds, Pageable pageable);
}