package com.example.matdongsanserver.domain.member.dto;

import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import lombok.*;

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

        @Builder
        public MemberDetail(Member member) {
            this.id = member.getId();
            this.email = member.getEmail();
            this.profileImage = member.getProfileImage();
            this.nickname = member.getNickname();
            this.role = member.getRole();
            this.followers = (long) member.getFollowerList().size();
            this.followings = (long) member.getFollowingList().size();
        }
    }
}
