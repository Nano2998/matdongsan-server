package com.example.matdongsanserver.domain.follow.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import com.example.matdongsanserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member following;  //팔로우를 거는 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private Member follower;  //팔로우를 받는 사람

    @Builder
    public Follow(Member follower, Member following) {
        this.follower = follower;
        this.following = following;
        // 양방향 관계 설정
        this.following.addFollowing(this);
        this.follower.addFollower(this);
    }
}
