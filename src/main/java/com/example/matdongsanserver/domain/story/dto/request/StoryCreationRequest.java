package com.example.matdongsanserver.domain.story.dto.request;

import com.example.matdongsanserver.domain.story.document.Language;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCreationRequest {
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
