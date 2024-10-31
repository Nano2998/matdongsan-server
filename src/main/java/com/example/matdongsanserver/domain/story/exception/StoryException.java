package com.example.matdongsanserver.domain.story.exception;

import com.example.matdongsanserver.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class StoryException extends BusinessException {
    private final StoryErrorCode storyErrorCode;

    public StoryException(StoryErrorCode storyErrorCode) {
        super(storyErrorCode);
        this.storyErrorCode = storyErrorCode;
    }
}
