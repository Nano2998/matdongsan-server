package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Follow;
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
    @Transactional
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
    public List<MemberDto.ChildDetail> getChildDetails(Long memberId) {
        return childRepository.findByMemberId(memberId)
                .stream()
                .map(MemberDto.ChildDetail::new)
                .toList();
    }

    /**
     * 팔로우
     */
    @Transactional
    public void follow(Long memberId, Long followerId) {
        if (followRepository.findByFollowingIdAndFollowerId(memberId, followerId).isPresent()) {
            throw new MemberException(MemberErrorCode.FOLLOW_ALREADY_EXISTS);
        }
        followRepository.save(Follow.builder()
                .following(memberRepository.findById(memberId).orElseThrow(
                        () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                ))
                .follower(memberRepository.findById(followerId).orElseThrow(
                        () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                ))
                .build());
    }

    /**
     * 팔로우 취소
     */
    @Transactional
    public void unfollow(Long memberId, Long followerId) {
        Follow follow = followRepository.findByFollowingIdAndFollowerId(memberId, followerId).orElseThrow(
                () ->  new MemberException(MemberErrorCode.FOLLOW_NOT_EXISTS)
        );

        follow.getFollower().getFollowingList().remove(follow);
        follow.getFollowing().getFollowerList().remove(follow);
        followRepository.delete(follow);
    }

    /**
     * 팔로우 리스트 조회
     */
}
