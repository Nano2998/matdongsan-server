package com.example.matdongsanserver.domain.story.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import com.example.matdongsanserver.domain.member.entity.Child;
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
    private Child child;

    private String storyId;

    @Builder
    public StoryQuestion(String storyId, Language language) {
        this.storyId = storyId;
        this.language = language;
    }

    public void updateChild(Child child) {
        this.child = child;
    }
}
