package com.example.matdongsanserver.domain.story.dto;

import com.example.matdongsanserver.domain.story.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class StoryDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryCreationRequest {
        private Language language;
        private int age;
        private String given;

        @JsonCreator
        public StoryCreationRequest(@JsonProperty("language") String language,
                                    @JsonProperty("age") int age,
                                    @JsonProperty("given") String given) {
            this.language = Language.valueOf(language.toUpperCase());
            this.age = age;
            this.given = given;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryUpdateRequest {
        private String title;
        private Boolean isPublic;
        private List<String> tags;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryCreationResponse {
        private String id;
        private String title;
        private String content;
        private String author;
        private String coverUrl;

        @Builder
        public StoryCreationResponse(Story story) {
            this.id = story.getId();
            this.title = story.getTitle();
            this.content = story.getContent();
            this.author = story.getAuthor();
            this.coverUrl = story.getCoverUrl();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StorySummary {
        private String id;
        private String title;
        private Long likes;
        private String coverUrl;
        private List<String> tags;
        private String author;

        public StorySummary(Story story) {
            this.id = story.getId();
            this.title = story.getTitle();
            this.likes = story.getLikes();
            this.coverUrl = story.getCoverUrl();
            this.tags = story.getTags();
            this.author = story.getAuthor();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryDetail extends StorySummary {
        private String content;
        private int age;
        private Language language;
        private List<String> tags;
        private LocalDateTime createdAt;
        private Boolean isLiked;

        @Builder
        public StoryDetail(Story story, Boolean isLiked) {
            super(story);
            this.content = story.getContent();
            this.age = story.getAge();
            this.language = story.getLanguage();
            this.tags = story.getTags();
            this.createdAt = story.getCreatedAt();
            this.isLiked = isLiked;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryTranslationResponse {
        private String id;
        private String translationTitle;
        private String translationContent;

        @Builder
        public StoryTranslationResponse(Story story) {
            this.id = story.getId();
            this.translationTitle = story.getTranslationTitle();
            this.translationContent = story.getTranslationContent();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
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
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class QnAs {
        private Long id;
        private String question;
        private String sampleAnswer;
        private String answer;

        @Builder
        public QnAs(QuestionAnswer questionAnswerPairs) {
            this.id = questionAnswerPairs.getId();
            this.question = questionAnswerPairs.getQuestion();
            this.sampleAnswer = questionAnswerPairs.getSampleAnswer();
            this.answer = questionAnswerPairs.getAnswer();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TTSCreationRequest {
        private String file_name;
        private String language;
        private String text;

        @Builder
        public TTSCreationRequest(String file_name, String language, String text) {
            this.file_name = file_name;
            this.language = language;
            this.text = text;
        }
    }
}
