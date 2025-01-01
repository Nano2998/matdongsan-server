package com.example.matdongsanserver.domain.dashboard.repository;

import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardErrorCode;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardException;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class StoryQuestionRepositoryTest {

    @Autowired
    private StoryQuestionRepository storyQuestionRepository;

    @Test
    @DisplayName("동화 질문 조회 성공")
    void findByIdOrThrow_Success() {
        // Given
        StoryQuestion storyQuestion = StoryQuestion.builder()
                .storyId("testStoryId")
                .language(Language.KO)
                .build();

        storyQuestionRepository.save(storyQuestion);

        // When
        StoryQuestion findStoryQuestion = storyQuestionRepository.findByIdOrThrow(storyQuestion.getId());

        // Then
        assertThat(findStoryQuestion.getStoryId()).isEqualTo("testStoryId");
    }

    @Test
    @DisplayName("동화 질문 조회 실패 - 존재하지 않는 동화 질문 조회")
    void findByIdOrThrow_Notfound() {
        // Given
        Long nonExistStoryQuestionId = -1L;

        // When
        DashboardException dashboardException = assertThrows(DashboardException.class, () ->
                storyQuestionRepository.findByIdOrThrow(nonExistStoryQuestionId)
        );

        // Then
        assertThat(dashboardException.getDashboardErrorCode())
                .isEqualTo(DashboardErrorCode.STORY_QUESTION_NOT_FOUND);
    }
}