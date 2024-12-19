package com.example.matdongsanserver.domain.story.entity.mongo;

import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;

public enum Language {
    EN,  // 영어
    KO;  // 한글

    // 대소문자 구분 없이 매칭 가능하도록 처리
    public static Language fromString(String value) {
        try {
            return Language.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE);
        }
    }
}
