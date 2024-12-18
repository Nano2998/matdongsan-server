package com.example.matdongsanserver.domain.follow.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.follow.exception.FollowErrorCode;
import com.example.matdongsanserver.domain.follow.exception.FollowException;
import com.example.matdongsanserver.domain.follow.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@DisplayName("팔로우 서비스 테스트")
class FollowServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FollowRepository followRepository;
    @Autowired
    FollowService followService;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll(); // 모든 데이터 삭제
    }

    @Test
    @DisplayName("작가 팔로우 성공")
    void follow() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();

        // When
        followService.follow(member.getId(),member2.getId());

        // Then
        assertThat(followRepository.findByFollowingId(member.getId()).get(0).getFollower().getId()).isEqualTo(member2.getId());
    }

    @Test
    @DisplayName("작가 팔로우 실패 - 이미 팔로우 한 작가")
    void follow2() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        followService.follow(member.getId(),member2.getId());

        // When
        FollowException followException = assertThrows(FollowException.class, () -> {
            followService.follow(member.getId(),member2.getId());
        });

        // Then
        assertThat(followException.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("작가 팔로우 해제 성공")
    void unfollow() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        followService.follow(member.getId(),member2.getId());

        // When
        followService.unfollow(member.getId(), member2.getId());

        // Then
        assertThat(member.getFollowerList().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("작가 팔로우 해제 실패 - 팔로우를 안한 사람")
    void unfollow2() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();

        // When
        FollowException followException = assertThrows(FollowException.class, () -> {
            followService.unfollow(member.getId(), member2.getId());
        });

        // Then
        assertThat(followException.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_NOT_EXISTS);
    }

    @Test
    @DisplayName("팔로우 리스트 조회 성공")
    void getFollowers() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        Member member3 = Member.builder()
                .email("test3@naver.com")
                .nickname("test3")
                .profileImage("test3Img")
                .role(Role.USER)
                .build();
        memberRepository.save(member3);

        // When
        followService.follow(member.getId(),member2.getId());
        followService.follow(member.getId(),member3.getId());
        List<MemberDto.MemberSummary> followers = followService.getFollowers(member.getId());

        // Then
        assertThat(followers.size()).isEqualTo(2);
        assertThat(followers.get(0).getNickname()).isEqualTo("test2");
        assertThat(followers.get(1).getNickname()).isEqualTo("test3");
    }

    // 멤버 추가 1
    private Member createAndSaveMember() {
        Member member = Member.builder()
                .email("test@naver.com")
                .nickname("test")
                .profileImage("testImg")
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    // 멤버 추가 2
    private Member createAndSaveMember2() {
        Member member = Member.builder()
                .email("test2@naver.com")
                .nickname("test2")
                .profileImage("test2Img")
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }
}