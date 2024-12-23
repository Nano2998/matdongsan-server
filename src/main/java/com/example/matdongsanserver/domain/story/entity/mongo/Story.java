package com.example.matdongsanserver.domain.story.entity.mongo;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    private String given; //입력 상황

    private String title; //제목

    private String content; //내용

    private String author;

    private String coverUrl; //표지 이미지

    private Boolean isPublic; //동화가 공개되었는지

    private List<String> tags; // 해시태그 리스트

    private String ttsUrl;  //TTS 저장 주소

    private Long likes;  //좋아요 수

    private Long memberId;  //생성한 멤버 아이디

    private List<Double> timestamps; // 해시태그 리스트

    /**
     * Redis 캐시를 위한 직렬화, 역직렬화 어노테이션 추가
     */
    @CreatedDate
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;

    @Builder
    public Story(int age, Language language, String given, String title, String content, String coverUrl, Long memberId, String author) {
        this.age = age;
        this.language = language;
        this.given = given;
        this.title = title;
        this.content = content;
        this.coverUrl = coverUrl;
        this.isPublic = true;
        this.tags = new ArrayList<>();
        this.ttsUrl = "";
        this.likes = 0L;
        this.memberId = memberId;
        this.author = author;
        this.timestamps = new ArrayList<>();
    }

    public Story updateStoryDetail(StoryDto.StoryUpdateRequest storyUpdateRequest) {
        this.title = storyUpdateRequest.getTitle();
        this.isPublic = storyUpdateRequest.getIsPublic();
        this.tags = storyUpdateRequest.getTags();
        return this;
    }

    public Story updateTTSUrl(String ttsUrl, List<Double> timestamps) {
        this.ttsUrl = ttsUrl;
        this.timestamps = timestamps;
        return this;
    }

    public Story addLikes() {
        this.likes ++;
        return this;
    }

    public Story removeLikes() {
        this.likes --;
        return this;
    }

    public void updateCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
