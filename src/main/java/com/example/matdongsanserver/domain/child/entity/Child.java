package com.example.matdongsanserver.domain.child.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import com.example.matdongsanserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Child extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_id")
    private Long id;

    private String name;

    private Integer englishAge;  //영어 연령

    private Integer koreanAge;  //한글 연령

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Child(String name, Integer englishAge, Integer koreanAge, Member member) {
        this.name = name;
        this.englishAge = englishAge;
        this.koreanAge = koreanAge;
        this.member = member;
        // 양방향 관계 설정
        this.member.addChild(this);
    }

    public void updateChild(String name, Integer englishAge, Integer koreanAge) {
        this.name = name;
        this.englishAge = englishAge;
        this.koreanAge = koreanAge;
    }
}
