package com.example.matdongsanserver.domain.story.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ParentDto {

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ParentQnaLogRequest {
        private String title;
        private String child;
        private LocalDateTime createAt;
    }
}
