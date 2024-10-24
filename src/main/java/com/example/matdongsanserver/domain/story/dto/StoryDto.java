package com.example.matdongsanserver.domain.story.dto;

import com.example.matdongsanserver.domain.story.document.Language;
import com.example.matdongsanserver.domain.story.document.Story;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

public class StoryDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StoryCreationRequest {
        private Language language;
        private int age;
        private String theme;

        @JsonCreator
        public StoryCreationRequest(@JsonProperty("language") String language,
                                    @JsonProperty("age") int age,
                                    @JsonProperty("theme") String theme) {
            this.language = Language.valueOf(language.toUpperCase());
            this.age = age;
            this.theme = theme;
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
}
