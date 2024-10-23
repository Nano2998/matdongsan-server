package com.example.matdongsanserver.domain.story.dto.response;

import com.example.matdongsanserver.domain.story.document.Story;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCreationResponse {
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
