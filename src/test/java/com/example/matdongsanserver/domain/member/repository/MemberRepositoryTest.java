package com.example.matdongsanserver.domain.member.repository;

import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 조회 성공")
    void findByIdOrThrow_Success() {
        // Given
        Member member = Member.builder()
                .email("test@email.com")
                .nickname("test")
                .profileImage("testImg")
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        // When
        Member findMember = memberRepository.findByIdOrThrow(member.getId());

        // Then
        assertThat(findMember.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않는 회원 조회")
    void findByIdOrThrow_Notfound() {
        // Given
        Long nonExistMemberId = -1L;

        // When
        MemberException memberException = assertThrows(MemberException.class, () ->
                memberRepository.findByIdOrThrow(nonExistMemberId)
        );

        // Then
        assertThat(memberException.getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}