package com.example.matdongsanserver.domain.story.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAnswerPair {

    private String question;
    private String sampleAnswer;
    private String answer;

    public void updateAnswer(String newAnswer) {
        this.answer = newAnswer;
    }

    @Builder
    public QuestionAnswerPair(String sampleAnswer, String question) {
        this.question = question;
        this.sampleAnswer = sampleAnswer;
    }
}
