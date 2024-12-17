package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryQuestionRepository extends JpaRepository<StoryQuestion, Long> {
    Page<StoryQuestion> findAllByChildIdIn(List<Long> childIds,Pageable pageable);

    Page<StoryQuestion> findByChildId(Long childId, Pageable pageable);
}
