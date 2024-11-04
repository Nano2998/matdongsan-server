package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
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
    public MemberDto.MemberDetail getMemberDetail(Long id) {
        return MemberDto.MemberDetail.builder()
                .member(memberRepository.findById(id).orElseThrow(
                        () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                ))
                .build();

    }

    /**
     * 자녀 생성
     */

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
