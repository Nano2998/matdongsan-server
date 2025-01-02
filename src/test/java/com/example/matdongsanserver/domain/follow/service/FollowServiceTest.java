package com.example.matdongsanserver.domain.follow.service;

import com.example.matdongsanserver.domain.follow.entity.Follow;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("팔로우 서비스 테스트")
class FollowServiceTest {

    @InjectMocks
    FollowService followService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    FollowRepository followRepository;

    @Test
    @DisplayName("작가 팔로우 성공")
    void follow() {
        // Given
        Member member = mock(Member.class);
        Member testMember = mock(Member.class);

        when(memberRepository.findByIdOrThrow(1L)).thenReturn(member);
        when(memberRepository.findByIdOrThrow(2L)).thenReturn(testMember);

        // When
        followService.follow(1L, 2L);

        // Then
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("작가 팔로우 실패 - 이미 팔로우 한 작가")
    void follow2() {
        // Given
        Member member = mock(Member.class);
        Member testMember = mock(Member.class);

        // Mock: 이미 팔로우 상태로 설정
        Follow mockFollow = mock(Follow.class);
        when(followRepository.findByFollowingIdAndFollowerId(1L, 2L))
                .thenReturn(Optional.of(mockFollow));

        // When & Then
        FollowException exception = assertThrows(
                FollowException.class,
                () -> followService.follow(1L, 2L) // 이미 팔로우한 경우 예외 발생
        );

        // Verify
        assertThat(exception.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("작가 팔로우 해제 성공")
    void unfollow() {
        Member member = mock(Member.class);
        Member testMember = mock(Member.class);

        Follow mockFollow = mock(Follow.class);
        when(mockFollow.getFollowing()).thenReturn(member);
        when(mockFollow.getFollower()).thenReturn(testMember);

        when(followRepository.findByFollowingIdAndFollowerId(1L, 2L))
                .thenReturn(Optional.of(mockFollow));

        // When
        followService.unfollow(1L, 2L);

        // Then
        verify(followRepository).delete(mockFollow); // 삭제 호출 검증
    }

    @Test
    @DisplayName("작가 팔로우 해제 실패 - 팔로우를 안한 사람")
    void unfollow2() {
        // Given
        Member member = mock(Member.class);
        Member testMember = mock(Member.class);

        // Mock: 팔로우 관계가 없는 상태로 설정
        when(followRepository.findByFollowingIdAndFollowerId(1L, 2L))
                .thenReturn(Optional.empty()); // 팔로우 관계 없음

        // When & Then
        FollowException exception = assertThrows(
                FollowException.class, // 예외 발생 기대
                () -> followService.unfollow(1L, 2L) // 팔로우 해제 시도
        );

        // Verify
        assertThat(exception.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_NOT_EXISTS);
    }
    @Test
    @DisplayName("팔로우 리스트 조회 성공")
    void getFollowers() {
        // Given
        Member member = mock(Member.class);
        Member testMember1 = mock(Member.class);
        Member testMember2 = mock(Member.class);

        Follow follow1 = mock(Follow.class);
        Follow follow2 = mock(Follow.class);

        // Mock 설정: 팔로우 관계
        when(follow1.getFollower()).thenReturn(testMember1);
        when(follow2.getFollower()).thenReturn(testMember2);

        List<Follow> mockFollowList = List.of(follow1, follow2);

        // Mock 설정: 팔로우 리스트 반환
        when(followRepository.findByFollowingId(1L)).thenReturn(mockFollowList);

        // Mock 설정: 멤버 정보
        when(testMember1.getNickname()).thenReturn("Follower1");
        when(testMember2.getNickname()).thenReturn("Follower2");

        // When
        List<MemberDto.MemberSummary> followers = followService.getFollowers(1L);

        // Then
        assertThat(followers.size()).isEqualTo(2);
        assertThat(followers.get(0).getNickname()).isEqualTo("Follower1");
        assertThat(followers.get(1).getNickname()).isEqualTo("Follower2");
    }

    // 멤버 추가
    private Member createMember(String email, String nickname, String profileImage) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .role(Role.USER)
                .build();
    }
}