package com.example.matdongsanserver.domain.member.dto;

import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import lombok.*;

public class MemberDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberDetail extends MemberSummary{
        private String email;
        private Role role;
        private Long followings;
        private Long storyCount;
        private Long likeCount;

        @Builder
        public MemberDetail(Member member, Long storyCount, Long likeCount) {
            super(member);
            this.email = member.getEmail();
            this.role = member.getRole();
            this.followings = (long) member.getFollowingList().size();
            this.storyCount = storyCount;
            this.likeCount = likeCount;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberDetailOther extends MemberSummary{
        private Role role;
        private Long followings;
        private Long storyCount;
        private Long likeCount;
        private Boolean isFollowed;

        @Builder
        public MemberDetailOther(Member member, Long storyCount, Long likeCount, Boolean isFollowed) {
            super(member);
            this.role = member.getRole();
            this.followings = (long) member.getFollowingList().size();
            this.storyCount = storyCount;
            this.likeCount = likeCount;
            this.isFollowed = isFollowed;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class MemberSummary {
        private Long id;
        private String profileImage;
        private String nickname;
        private Long followers;

        public MemberSummary(Member member) {
            this.id = member.getId();
            this.profileImage = member.getProfileImage();
            this.nickname = member.getNickname();
            this.followers = (long) member.getFollowerList().size();
        }
    }
}
