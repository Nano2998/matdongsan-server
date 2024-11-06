package com.example.matdongsanserver.domain.member.dto;

import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import lombok.*;

import java.time.LocalDate;

public class MemberDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberCreationRequest {
        private String email;
        private String profileImage;
        private String nickname;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberDetail {
        private Long id;
        private String email;
        private String profileImage;
        private String nickname;
        private Role role;
        private Long followers;
        private Long followings;
        private Long storyCount;

        @Builder
        public MemberDetail(Member member, Long storyCount) {
            this.id = member.getId();
            this.email = member.getEmail();
            this.profileImage = member.getProfileImage();
            this.nickname = member.getNickname();
            this.role = member.getRole();
            this.followers = (long) member.getFollowerList().size();
            this.followings = (long) member.getFollowingList().size();
            this.storyCount = storyCount;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChildCreationRequest {
        private String name;
        private String nickname;
        private LocalDate birthday;
        private Integer englishAge;
        private Integer koreanAge;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChildDetail {
        private Long id;
        private String name;
        private String nickname;
        private LocalDate birthday;
        private Integer englishAge;
        private Integer koreanAge;

        @Builder
        public ChildDetail(Child child) {
            this.id = child.getId();
            this.name = child.getName();
            this.nickname = child.getNickname();
            this.birthday = child.getBirthday();
            this.englishAge = child.getEnglishAge();
            this.koreanAge = child.getKoreanAge();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberSummary {
        private Long id;
        private String profileImage;
        private String nickname;
        private Long followers;

        @Builder
        public MemberSummary(Member member) {
            this.id = member.getId();
            this.profileImage = member.getProfileImage();
            this.nickname = member.getNickname();
            this.followers = (long) member.getFollowerList().size();
        }
    }
}
