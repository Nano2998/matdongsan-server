package com.example.matdongsanserver.domain.child.dto;

import com.example.matdongsanserver.domain.child.entity.Child;
import lombok.*;

public class ChildDto {

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChildRequest {
        private String name;
        private Integer englishAge;
        private Integer koreanAge;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChildDetail {
        private Long id;
        private String name;
        private Integer englishAge;
        private Integer koreanAge;

        @Builder
        public ChildDetail(Child child) {
            this.id = child.getId();
            this.name = child.getName();
            this.englishAge = child.getEnglishAge();
            this.koreanAge = child.getKoreanAge();
        }
    }
}
