package com.example.matdongsanserver.domain.story.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
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

    @Enumerated(EnumType.STRING)
    private Language language;

    @OneToMany(mappedBy = "storyQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> questionAnswers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;  // 추후에 자녀에게 저장하도록 수정 고려

    private String storyId;

    @Builder
    public StoryQuestion(Child child, String storyId, Language language) {
        this.child = child;
        this.storyId = storyId;
        this.language = language;
    }
}
