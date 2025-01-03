package com.example.matdongsanserver.domain.child.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("자녀 서비스 테스트")
class ChildServiceTest {

    @InjectMocks
    ChildService childService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    ChildRepository childRepository;

    @Test
    @DisplayName("자녀 추가 성공")
    void registerChild() {
        //Given
        Member member = mock(Member.class);

        ChildDto.ChildRequest childRequest = ChildDto.ChildRequest.builder()
                .name("nano")
                .englishAge(5)
                .koreanAge(6)
                .build();

        Child savedChild = Child.builder()
                .member(member)
                .name("nano")
                .englishAge(5)
                .koreanAge(6)
                .build();

        when(memberRepository.findByIdOrThrow(1L)).thenReturn(member);
        when(childRepository.save(any(Child.class))).thenReturn(savedChild);
        when(childRepository.findByMemberId(1L)).thenReturn(List.of(savedChild));

        //When
        List<ChildDto.ChildDetail> children = childService.registerChild(1L, childRequest);

        //Then
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("nano");
        assertThat(children.get(0).getEnglishAge()).isEqualTo(5);
        assertThat(children.get(0).getKoreanAge()).isEqualTo(6);
    }

    @Test
    @DisplayName("자녀 삭제 성공")
    void deleteChild() {
        // Given
        Member member = mock(Member.class);
        List<Child> children = new ArrayList<>();
        Child child = mock(Child.class);
        Child child2 = mock(Child.class);

        // 자녀 리스트 설정
        when(member.getChildren()).thenReturn(children); // 초기 빈 리스트 설정
        children.add(child); // 자녀 추가
        children.add(child2); // 자녀 추가

        given(memberRepository.findByIdOrThrow(1L)).willReturn(member);
        given(childRepository.findByIdOrThrow(1L)).willReturn(child);

        // When
        childService.deleteChild(1L, 1L);
        children.remove(child);

        // Then
        assertThat(children.size()).isEqualTo(1);
        verify(childRepository).delete(child); // 삭제 호출 확인
    }


    @Test
    @DisplayName("자녀 정보 수정 성공")
    void updateChild() {
        // Given
        Member member = mock(Member.class);
        Child child = mock(Child.class);

        List<Child> children = new ArrayList<>();
        children.add(child); // 자녀 추가

        // Mock 설정: Member
        when(member.getChildren()).thenReturn(children); // 회원의 자녀 리스트 설정

        // Mock 설정: Child
        when(child.getId()).thenReturn(1L);
        when(child.getName()).thenReturn("test"); // 초기 이름
        when(child.getEnglishAge()).thenReturn(5); // 초기 영어 나이
        when(child.getKoreanAge()).thenReturn(6); // 초기 한국 나이

        // 수정 요청 데이터
        ChildDto.ChildRequest updatedRequest = ChildDto.ChildRequest.builder()
                .name("nano")
                .englishAge(7)
                .koreanAge(8)
                .build();

        given(memberRepository.findByIdOrThrow(1L)).willReturn(member);
        given(childRepository.findByIdOrThrow(1L)).willReturn(child);

        // Mock 설정: 자녀 정보 업데이트
        doAnswer(invocation -> {
            when(child.getName()).thenReturn(updatedRequest.getName());
            when(child.getEnglishAge()).thenReturn(updatedRequest.getEnglishAge());
            when(child.getKoreanAge()).thenReturn(updatedRequest.getKoreanAge());
            return null;
        }).when(child).updateChild(anyString(), anyInt(), anyInt());

        given(childRepository.findByMemberId(1L)).willReturn(List.of(child));

        // When
        List<ChildDto.ChildDetail> updatedDetails = childService.updateChild(1L, 1L, updatedRequest);

        // Then
        assertThat(updatedDetails).isNotEmpty(); // 리스트 비어 있지 않은지 확인
        assertThat(updatedDetails.get(0).getName()).isEqualTo("nano"); // 이름 검증
    }
}