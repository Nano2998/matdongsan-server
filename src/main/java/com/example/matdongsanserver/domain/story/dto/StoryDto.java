package com.example.matdongsanserver.domain.story.dto;

import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class StoryDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class StoryCreationRequest {
        private String language;
        private int age;
        private String given;
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class StoryUpdateRequest {
        private String title;
        private Boolean isPublic;
        private List<String> tags;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
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
    @AllArgsConstructor
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
        private Long authorId;
        private Boolean isFollowed;
        private Boolean isMyStory;

        @Builder
        public StoryDetail(Story story, Boolean isLiked, Boolean isFollowed, Boolean isMyStory) {
            super(story);
            this.content = story.getContent();
            this.age = story.getAge();
            this.language = story.getLanguage();
            this.tags = story.getTags();
            this.createdAt = story.getCreatedAt();
            this.isLiked = isLiked;
            this.authorId = story.getMemberId();
            this.isFollowed = isFollowed;
            this.isMyStory = isMyStory;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TTSCreationRequest {
        private String file_name;
        private String language;
        private String text;
        private String folder;

        @Builder
        public TTSCreationRequest(String file_name, String language, String text, String folder) {
            this.file_name = file_name;
            this.language = language;
            this.text = text;
            this.folder = folder;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TTSResponse {
        private String ttsUrl;
        private List<Double> timestamps;

        @Builder
        public TTSResponse(String ttsUrl, List<Double> timestamps) {
            this.ttsUrl = ttsUrl;
            this.timestamps = timestamps;
        }
    }
}
