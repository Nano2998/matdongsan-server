package com.example.matdongsanserver.domain.dashboard.dto;

import lombok.*;

import java.time.LocalDateTime;

public class DashboardDto {

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ParentQnaLogRequest {
        private Long id;
        private String title;
        private String child;
        private LocalDateTime createAt;
    }
}
