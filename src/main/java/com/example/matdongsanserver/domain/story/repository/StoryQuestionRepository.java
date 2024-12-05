package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryQuestionRepository extends JpaRepository<StoryQuestion, Long> {
}
