package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
}
