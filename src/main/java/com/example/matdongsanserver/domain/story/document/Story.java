package com.example.matdongsanserver.domain.story.document;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "story")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Story {

    @Id
    private String id;

    private int age; //입력 나이

    private Language language; //입력 언어

    private String theme; //입력 상황

    private String title; //제목

    private String content; //내용

    private Long views; //조회수

    private String coverUrl; //표지 이미지

    private Boolean isPublic; //동화가 공개되었는지

    private List<String> tags; // 해시태그 리스트 추가

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Story(int age, Language language, String theme, String title, String content, String coverUrl) {
        this.age = age;
        this.language = language;
        this.theme = theme;
        this.title = title;
        this.content = content;
        this.views = 0L;
        this.coverUrl = coverUrl;
        this.isPublic = false;
        this.tags = new ArrayList<>();
    }

    public Story updateStoryDetail(StoryDto.StoryUpdateRequest storyUpdateRequest) {
        this.title = storyUpdateRequest.getTitle();
        this.isPublic = storyUpdateRequest.getIsPublic();
        this.tags = storyUpdateRequest.getTags();
        return this;
    }
}
