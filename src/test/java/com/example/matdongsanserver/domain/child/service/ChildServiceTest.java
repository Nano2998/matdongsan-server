package com.example.matdongsanserver.domain.child.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.child.exception.ChildErrorCode;
import com.example.matdongsanserver.domain.child.exception.ChildException;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
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
@DisplayName("자녀 서비스 테스트")
class ChildServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    ChildService childService;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll(); // 모든 데이터 삭제
    }

    @Test
    @DisplayName("자녀 추가 성공")
    void registerChild() {
        //Given
        Member member = createAndSaveMember();
        ChildDto.ChildRequest childRequest = createChildRequest("테스트", 4, 4);

        //When
        List<ChildDto.ChildDetail> registerChild = childService.registerChild(member.getId(), childRequest);

        //Then
        List<Child> children = childRepository.findByMemberId(member.getId());
        assertThat(children).hasSize(1);
    }

    @Test
    @DisplayName("자녀 삭제 성공")
    void deleteChild() {
        // Given
        Member member = createAndSaveMember();
        ChildDto.ChildRequest childRequest1 = createChildRequest("테스트1", 4, 4);
        ChildDto.ChildRequest childRequest2 = createChildRequest("테스트2", 3, 3);

        // When
        List<ChildDto.ChildDetail> registerFirstChild = childService.registerChild(member.getId(), childRequest1);
        List<ChildDto.ChildDetail> registerSecondChild = childService.registerChild(member.getId(), childRequest2);
        assertThat(childService.getChildDetails(member.getId())).hasSize(2);
        childService.deleteChild(member.getId(),registerSecondChild.get(0).getId());

        // Then
        assertThat(childService.getChildDetails(member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("자녀 삭제 실패 - 존재하지 않는 자녀")
    void deleteChild2() {
        // Given
        Member member = createAndSaveMember();
        Long nonExistChildId = 1L;

        // When & Then
        ChildException childException = assertThrows(ChildException.class, () -> {
            childService.deleteChild(member.getId(), nonExistChildId);
        });
        assertThat(childException.getErrorCode()).isEqualTo(ChildErrorCode.CHILD_NOT_FOUND);
    }

    @Test
    @DisplayName("자녀 삭제 실패 - 존재하지 않는 멤버")
    void deleteChild3() {
        // Given
        Long nonExistMemberId = 1L;
        ChildDto.ChildRequest childRequest1 = createChildRequest("테스트", 4, 4);

        // When & Then
        MemberException memberException = assertThrows(MemberException.class, () -> {
            childService.registerChild(nonExistMemberId,childRequest1);
        });
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("자녀 정보 수정 성공")
    void updateChild() {
        // Given
        Member member = createAndSaveMember();
        ChildDto.ChildRequest childRequest = createChildRequest("test", 4, 4);
        List<ChildDto.ChildDetail> registerChild = childService.registerChild(member.getId(), childRequest);
        ChildDto.ChildRequest childRequest2 = createChildRequest("NameChange", 4, 4);

        // When
        List<ChildDto.ChildDetail> childDetails = childService.updateChild(member.getId(), registerChild.get(0).getId(), childRequest2);

        // Then
        assertThat(childDetails.get(0).getName()).isEqualTo("NameChange");
    }

    @Test
    @DisplayName("자녀 정보 수정 실패 - 나이 조건이 맞지 않음")
    void updateChild2() {
        // Given
        Member member = createAndSaveMember();
        ChildDto.ChildRequest childRequest = createChildRequest("test", 2, 2);

        // When
        ChildException childException = assertThrows(ChildException.class, () -> {
            childService.registerChild(member.getId(), childRequest);
        });

        // Then
        assertThat(childException.getErrorCode()).isEqualTo(ChildErrorCode.INVALID_AGE);
    }

    @Test
    @DisplayName("자녀 정보 수정 실패 - 수정 삭제 권한이 없음")
    void updateChild3() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();

        ChildDto.ChildRequest childRequest = createChildRequest("test", 4, 4);
        List<ChildDto.ChildDetail> registerChild = childService.registerChild(member.getId(), childRequest);
        ChildDto.ChildRequest childRequest2 = createChildRequest("NameChange", 4, 4);

        // When
        ChildException childException = assertThrows(ChildException.class, () -> {
            childService.updateChild(member2.getId(),registerChild.get(0).getId(),childRequest2);
        });

        // Then
        assertThat(childException.getErrorCode()).isEqualTo(ChildErrorCode.CANNOT_ACCESS_CHILD);
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

    // 자녀 추가 1
    private ChildDto.ChildRequest createChildRequest(String name, int englishAge, int koreanAge) {
        return ChildDto.ChildRequest.builder()
                .name(name)
                .englishAge(englishAge)
                .koreanAge(koreanAge)
                .build();
    }
}