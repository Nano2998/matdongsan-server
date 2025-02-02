package com.example.matdongsanserver.domain.dashboard.dto;

import com.example.matdongsanserver.domain.dashboard.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class StoryQuestionResponse {
        private Long id;
        private String storyId;
        private Language language;
        private List<QnAs> qnAs;

        @Builder
        public StoryQuestionResponse(StoryQuestion storyquestion) {
            this.id = storyquestion.getId();
            this.language = storyquestion.getLanguage();
            this.storyId = storyquestion.getStoryId();
            this.qnAs = storyquestion.getQuestionAnswers()
                    .stream()
                    .map(QnAs::new)
                    .toList();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class QnAs {
        private Long id;
        private String question;
        private String sampleAnswer;
        private String answer;

        @Builder
        public QnAs(QuestionAnswer questionAnswer) {
            this.id = questionAnswer.getId();
            this.question = questionAnswer.getQuestion();
            this.sampleAnswer = questionAnswer.getSampleAnswer();
            this.answer = questionAnswer.getAnswer();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ParentQnaLogResponse {
        private Long id;
        private String title;
        private String child;
        private LocalDateTime createAt;
    }
}
