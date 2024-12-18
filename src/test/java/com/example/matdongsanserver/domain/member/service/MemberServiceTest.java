package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.follow.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
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
@DisplayName("멤버 서비스 테스트")
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    StoryRepository storyRepository;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    FollowRepository followRepository;


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


    @Test
    @DisplayName("자녀 추가 성공")
    void registerChild() {
        //Given
        Member member = createAndSaveMember();
        MemberDto.ChildRequest childRequest = createChildRequest("테스트", 4, 4);

        //When
        List<MemberDto.ChildDetail> registerChild = memberService.registerChild(member.getId(), childRequest);

        //Then
        List<Child> children = childRepository.findByMemberId(member.getId());
        assertThat(children).hasSize(1);
    }

    @Test
    @DisplayName("자녀 삭제 성공")
    void deleteChild() {
        // Given
        Member member = createAndSaveMember();
        MemberDto.ChildRequest childRequest1 = createChildRequest("테스트1", 4, 4);
        MemberDto.ChildRequest childRequest2 = createChildRequest("테스트2", 3, 3);

        // When
        List<MemberDto.ChildDetail> registerFirstChild = memberService.registerChild(member.getId(), childRequest1);
        List<MemberDto.ChildDetail> registerSecondChild = memberService.registerChild(member.getId(), childRequest2);
        assertThat(memberService.getChildDetails(member.getId())).hasSize(2);
        memberService.deleteChild(member.getId(),registerSecondChild.get(0).getId());

        // Then
        assertThat(memberService.getChildDetails(member.getId())).hasSize(1);
    }

    @Test
    @DisplayName("자녀 삭제 실패 - 존재하지 않는 자녀")
    void deleteChild2() {
        // Given
        Member member = createAndSaveMember();
        Long nonExistChildId = 1L;

        // When & Then
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.deleteChild(member.getId(), nonExistChildId);
        });
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.CHILD_NOT_FOUND);
    }

    @Test
    @DisplayName("자녀 삭제 실패 - 존재하지 않는 멤버")
    void deleteChild3() {
        // Given
    	Long nonExistMemberId = 1L;
        MemberDto.ChildRequest childRequest1 = createChildRequest("테스트", 4, 4);

        // When & Then
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.registerChild(nonExistMemberId,childRequest1);
        });
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("자녀 정보 수정 성공")
    void updateChild() {
        // Given
        Member member = createAndSaveMember();
        MemberDto.ChildRequest childRequest = createChildRequest("test", 4, 4);
        List<MemberDto.ChildDetail> registerChild = memberService.registerChild(member.getId(), childRequest);
        MemberDto.ChildRequest childRequest2 = createChildRequest("NameChange", 4, 4);

        // When
        List<MemberDto.ChildDetail> childDetails = memberService.updateChild(member.getId(), registerChild.get(0).getId(), childRequest2);

        // Then
        assertThat(childDetails.get(0).getName()).isEqualTo("NameChange");
    }

    @Test
    @DisplayName("자녀 정보 수정 실패 - 나이 조건이 맞지 않음")
    void updateChild2() {
        // Given
        Member member = createAndSaveMember();
        MemberDto.ChildRequest childRequest = createChildRequest("test", 2, 2);

        // When
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.registerChild(member.getId(), childRequest);
        });

        // Then
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_AGE);
    }

    @Test
    @DisplayName("자녀 정보 수정 실패 - 수정 삭제 권한이 없음")
    void updateChild3() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();

        MemberDto.ChildRequest childRequest = createChildRequest("test", 4, 4);
        List<MemberDto.ChildDetail> registerChild = memberService.registerChild(member.getId(), childRequest);
        MemberDto.ChildRequest childRequest2 = createChildRequest("NameChange", 4, 4);

        // When
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.updateChild(member2.getId(),registerChild.get(0).getId(),childRequest2);
        });

        // Then
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.CANNOT_ACCESS_CHILD);
    }

    @Test
    @DisplayName("작가 팔로우 성공")
    void follow() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();

        // When
        memberService.follow(member.getId(),member2.getId());

        // Then
        assertThat(followRepository.findByFollowingId(member.getId()).get(0).getFollower().getId()).isEqualTo(member2.getId());
    }

    @Test
    @DisplayName("작가 팔로우 실패 - 이미 팔로우 한 작가")
    void follow2() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        memberService.follow(member.getId(),member2.getId());

        // When
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.follow(member.getId(),member2.getId());
        });

        // Then
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.FOLLOW_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("작가 팔로우 해제 성공")
    void unfollow() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        memberService.follow(member.getId(),member2.getId());

        // When
        memberService.unfollow(member.getId(), member2.getId());

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
        MemberException memberException = assertThrows(MemberException.class, () -> {
            memberService.unfollow(member.getId(), member2.getId());
        });

        // Then
        assertThat(memberException.getErrorCode()).isEqualTo(MemberErrorCode.FOLLOW_NOT_EXISTS);
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
        memberService.follow(member.getId(),member2.getId());
        memberService.follow(member.getId(),member3.getId());
        List<MemberDto.MemberSummary> followers = memberService.getFollowers(member.getId());

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

    // 자녀 추가 1
    private MemberDto.ChildRequest createChildRequest(String name, int englishAge, int koreanAge) {
        return MemberDto.ChildRequest.builder()
                .name(name)
                .englishAge(englishAge)
                .koreanAge(koreanAge)
                .build();
    }
}