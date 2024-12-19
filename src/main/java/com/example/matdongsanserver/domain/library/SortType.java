package com.example.matdongsanserver.domain.library;

import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;

public enum SortType {
    RECENT, // 최신순
    POPULAR; // 인기순

    // 대소문자 구분 없이 매칭 가능하도록 처리
    public static SortType fromString(String value) {
        try {
            return SortType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StoryException(StoryErrorCode.INVALID_SORT_TYPE);
        }
    }
}