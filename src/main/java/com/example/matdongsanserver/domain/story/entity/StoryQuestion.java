package com.example.matdongsanserver.domain.story.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import com.example.matdongsanserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_question_id")
    private Long id;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "question_answer_pairs", joinColumns = @JoinColumn(name = "story_question_id"))
    private List<QuestionAnswerPair> questionAnswerPairs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 추후에 자녀에게 저장하도록 수정 고려

    @Builder
    public StoryQuestion(List<QuestionAnswerPair> questionAnswerPairs, Member member) {
        this.questionAnswerPairs = questionAnswerPairs;
        this.member = member;
    }
}
