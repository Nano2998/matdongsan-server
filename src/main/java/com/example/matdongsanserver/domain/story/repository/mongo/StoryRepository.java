package com.example.matdongsanserver.domain.story.repository.mongo;

import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    List<Story> findByIsPublicTrueOrderByCreatedAtDesc();
    List<Story> findByIsPublicTrueOrderByLikesDesc();
    List<Story> findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(Long memberId);
    List<Story> findByIsPublicTrueAndMemberIdOrderByLikesDesc(Long memberId);
    List<Story> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    List<Story> findByMemberIdOrderByLikesDesc(Long memberId);
    List<Story> findByIdIn(List<String> storyIds);
}
