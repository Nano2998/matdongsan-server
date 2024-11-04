package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final FollowRepository followRepository;

    /**
     * 멤버 생성 - 로그인 도입시 수정 예정
     */
    @Transactional
    public MemberDto.MemberDetail registerMember(MemberDto.MemberCreationRequest memberCreationRequest) {
        return MemberDto.MemberDetail.builder()
                .member(memberRepository.save(Member.builder()
                        .email(memberCreationRequest.getEmail())
                        .profileImage(memberCreationRequest.getProfileImage())
                        .role(Role.USER)
                        .nickname(memberCreationRequest.getNickname())
                        .profileImage(memberCreationRequest.getProfileImage())
                        .build()))
                .build();
    }

    /**
     * 맴버 조회
     */
    public MemberDto.MemberDetail getMemberDetail(Long memberId) {
        return MemberDto.MemberDetail.builder()
                .member(memberRepository.findById(memberId).orElseThrow(
                        () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                ))
                .build();

    }

    /**
     * 자녀 생성
     */
    public List<MemberDto.ChildDetail> registerChild(Long memberId, List<MemberDto.ChildCreationRequest> childCreationRequests) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        List<MemberDto.ChildDetail> childDetails = new ArrayList<>();

        childCreationRequests.forEach(childCreationRequest -> {
            childDetails.add(MemberDto.ChildDetail.builder()
                    .child(childRepository.save(Child.builder()
                            .member(member)
                            .name(childCreationRequest.getName())
                            .birthday(childCreationRequest.getBirthday())
                            .englishAge(childCreationRequest.getEnglishAge())
                            .koreanAge(childCreationRequest.getKoreanAge())
                            .nickname(childCreationRequest.getNickname())
                            .build()))
                    .build());
        });
        return childDetails;
    }

    /**
     * 자녀 조회
     */

    /**
     * 팔로우
     */

    /**
     * 팔로우 취소
     */
}
