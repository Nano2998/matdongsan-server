package com.example.matdongsanserver.domain.story;

import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import lombok.Getter;

@Getter
public enum AgeType {

    MAIN(3, 8), // 3~8세
    LV1(3, 4), // 3~4세
    LV2(5, 6), // 5~6세
    LV3(7, 8); // 7~8세

    private final int startAge;
    private final int endAge;

    AgeType(int startAge, int endAge) {
        this.startAge = startAge;
        this.endAge = endAge;
    }

    public static AgeType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "main" -> MAIN;
            case "lv1" -> LV1;
            case "lv2" -> LV2;
            case "lv3" -> LV3;
            default -> throw new StoryException(StoryErrorCode.INVALID_AGE_TYPE);
        };
    }

}