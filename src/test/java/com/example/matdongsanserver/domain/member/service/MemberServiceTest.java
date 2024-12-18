package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("멤버 서비스 테스트")
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    StoryRepository storyRepository;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll(); // 모든 데이터 삭제
        storyRepository.deleteAll(); // 모든 동화 삭제
    }

    @Test
    @DisplayName("멤버 조회 성공")
    void getMemberDetail() {
        // Given
        Member member = createAndSaveMember();

        // When
        MemberDto.MemberDetail memberDetail = memberService.getMemberDetail(member.getId());

        // Then
        assertThat(memberDetail.getEmail()).isEqualTo("test@naver.com");
        assertThat(memberDetail.getStoryCount()).isEqualTo(0);
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
}