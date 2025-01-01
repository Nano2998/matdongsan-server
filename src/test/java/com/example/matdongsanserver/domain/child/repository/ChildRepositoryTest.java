package com.example.matdongsanserver.domain.child.repository;

import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.child.exception.ChildErrorCode;
import com.example.matdongsanserver.domain.child.exception.ChildException;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class ChildRepositoryTest {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("자녀 조회 성공")
    void findByIdOrThrow_Success() {
        // Given
        Member member = Member.builder()
                .email("test@email.com")
                .nickname("test")
                .profileImage("testImg")
                .role(Role.USER)
                .build();

        Child child = Child.builder()
                .member(member)
                .englishAge(3)
                .koreanAge(3)
                .name("testChild")
                .build();

        memberRepository.save(member);
        childRepository.save(child);

        // When
        Child findChild = childRepository.findByIdOrThrow(child.getId());

        // Then
        assertThat(findChild.getName()).isEqualTo("testChild");
    }

    @Test
    @DisplayName("자녀 조회 실패 - 존재하지 않는 자녀 조회")
    void findByIdOrThrow_NotFound() {
        // Given
        Long nonExistChildId = -1L;

        // When
        ChildException childException = assertThrows(ChildException.class, () ->
                childRepository.findByIdOrThrow(nonExistChildId)
        );

        // Then
        assertThat(childException.getChildErrorCode())
                .isEqualTo(ChildErrorCode.CHILD_NOT_FOUND);
    }
}