package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    Optional<StoryLike> findByStoryIdAndMemberId(String storyId, Long memberId);
    List<StoryLike> findByMemberId(Long memberId);
}
