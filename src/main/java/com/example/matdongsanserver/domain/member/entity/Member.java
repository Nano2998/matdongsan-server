package com.example.matdongsanserver.domain.member.entity;

import com.example.matdongsanserver.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String profileImage;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> children =  new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followingList = new ArrayList<>();  // 내가 팔로우하는 사람들

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followerList = new ArrayList<>();   // 나를 팔로우하는 사람들

    @Builder
    public Member(String email, String profileImage, String nickname, Role role) {
        this.email = email;
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.role = role;
    }

    public void addChild(Child child) {
        this.children.add(child);
    }

    public void addFollowing(Follow follow) {
        this.followingList.add(follow);
    }

    public void addFollower(Follow follow) {
        this.followerList.add(follow);
    }

    /**
     * children이 비어있는지 검사하는 메서드
     */
    public boolean hasNoChildren() {
        return children.isEmpty();
    }
}