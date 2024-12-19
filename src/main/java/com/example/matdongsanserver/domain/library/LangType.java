package com.example.matdongsanserver.domain.library;

import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;

public enum LangType {
    ALL, // 한국어, 영어
    KO, // 한국어
    EN; // 영어

    // 대소문자 구분 없이 매칭 가능하도록 처리
    public static LangType fromString(String value) {
        try {
            return LangType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE_TYPE);
        }
    }
}
