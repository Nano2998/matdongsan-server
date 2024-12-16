package com.example.matdongsanserver.domain.story.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_answer_id")
    private Long id;

    private String question;
    private String sampleAnswer;
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_question_id")
    private StoryQuestion storyQuestion;

    @Builder
    public QuestionAnswer(String question, String sampleAnswer, StoryQuestion storyQuestion) {
        this.question = question;
        this.sampleAnswer = sampleAnswer;
        this.storyQuestion = storyQuestion;

        storyQuestion.getQuestionAnswers().add(this);
    }

    public String updateAnswer(String answer) {
        this.answer = answer;
        return answer;
    }
}
