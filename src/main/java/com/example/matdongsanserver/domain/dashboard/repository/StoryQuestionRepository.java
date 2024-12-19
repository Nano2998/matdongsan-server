package com.example.matdongsanserver.domain.dashboard.repository;

import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardErrorCode;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryQuestionRepository extends JpaRepository<StoryQuestion, Long> {
    default StoryQuestion findByIdOrThrow(Long storyQuestionId) {
        return findById(storyQuestionId).orElseThrow(
                () -> new DashboardException(DashboardErrorCode.STORY_QUESTION_NOT_FOUND)
        );
    }
    Page<StoryQuestion> findAllByChildIdIn(List<Long> childIds,Pageable pageable);
    Page<StoryQuestion> findByChildId(Long childId, Pageable pageable);
}
