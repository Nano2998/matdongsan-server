package com.example.matdongsanserver.domain.story.dto;

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

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryUpdateRequest {
        private String title;
        private Boolean isPublic;
        private List<String> tags;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatGptResponse {
        private String id;
        private List<ChoiceResponse> choices;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChoiceResponse {
        private MessageResponse message;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageResponse {
        private String role;
        private String content;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryCreationResponse {
        private String id;
        private String title;
        private String content;

        @Builder
        public StoryCreationResponse(Story story) {
            this.id = story.getId();
            this.title = story.getTitle();
            this.content = story.getContent();
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

        public StorySummary(Story story) {
            this.id = story.getId();
            this.title = story.getTitle();
            this.likes = story.getLikes();
            this.coverUrl = story.getCoverUrl();
            this.tags = story.getTags();
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

        @Builder
        public StoryDetail(Story story) {
            super(story);
            this.content = story.getContent();
            this.age = story.getAge();
            this.language = story.getLanguage();
            this.tags = story.getTags();
            this.createdAt = story.getCreatedAt();
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
}
