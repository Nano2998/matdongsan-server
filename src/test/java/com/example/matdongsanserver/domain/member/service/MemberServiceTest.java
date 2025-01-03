package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.follow.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("멤버 서비스 테스트")
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    StoryRepository storyRepository;
    @Mock
    FollowRepository followRepository;

    @Test
    @DisplayName("멤버 조회 성공")
    void getMemberDetail(){
        //Given
        Member member = createMockMember();
        when(memberRepository.findByIdOrThrow(1L)).thenReturn(member);

        //When
        MemberDto.MemberDetail memberDetail = memberService.getMemberDetail(1L);

        //Then
        assertThat(memberDetail.getEmail()).isEqualTo("test@naver.com");
    }

    @Test
    @DisplayName("멤버 조회 실패")
    void getMemberDetail2() {
        // Given
        Member member = createMockMember();
        when(memberRepository.findByIdOrThrow(999L)).thenThrow(new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // When & Then
        assertThrows(MemberException.class, () -> {
            memberService.getMemberDetail(999L);
        });

    }

    @Test
    @DisplayName("다른 작가의 정보 조회 성공")
    void getOtherMemberDetail() {
        // Given
        Member member = createMockMember();
        Member member2 = createMockMember2();

        when(memberRepository.findByIdOrThrow(member.getId())).thenReturn(member);
        when(memberRepository.findByIdOrThrow(member2.getId())).thenReturn(member2);
        // When
        MemberDto.MemberDetailOther otherMemberDetail = memberService.getOtherMemberDetail(member.getId(), member2.getId());

        // Then
        assertThat(otherMemberDetail.getNickname()).isEqualTo("test2");
    }
    // 멤버 추가 1
    private Member createMockMember() {
        return Member.builder()
                .email("test@naver.com")
                .nickname("test")
                .profileImage("testImg")
                .role(Role.USER)
                .build();
    }

    // 멤버 추가 2
    private Member createMockMember2() {
        return Member.builder()
                .email("test2@naver.com")
                .nickname("test2")
                .profileImage("test2Img")
                .role(Role.USER)
                .build();
    }
}