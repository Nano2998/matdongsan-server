package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.common.utils.S3Utils;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Follow;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final S3Utils s3Utils;
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final FollowRepository followRepository;
    private final StoryRepository storyRepository;

    /**
     * 회원가입 이후 닉네임 및 프로필 이미지 등록 로직
     */
    @Transactional
    public MemberDto.MemberDetail updateMember(Long memberId, String nickname, MultipartFile profileImage) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );
        findMember.updateNickname(nickname);

        findMember.updateProfileImage(Optional.ofNullable(profileImage)
                .filter(file -> !file.isEmpty())
                .map(file -> s3Utils.uploadFile("profiles/", String.valueOf(memberId), file))
                .orElse(null));

        return MemberDto.MemberDetail.builder()
                .member(findMember)
                .storyCount(storyRepository.countByMemberId(memberId))
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
                .storyCount(storyRepository.countByMemberId(memberId))
                .build();
    }

    /**
     * 자녀 추가 -> 전체 자녀를 반환
     */
    @Transactional
    public List<MemberDto.ChildDetail> registerChild(Long memberId, MemberDto.ChildRequest childRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        // 나이 검사
        if (childRequest.getEnglishAge() < 3 || childRequest.getEnglishAge() > 8 ||
                childRequest.getKoreanAge() < 3 || childRequest.getKoreanAge() > 8) {
            throw new MemberException(MemberErrorCode.INVALID_AGE);
        }
        childRepository.save(Child.builder()
                .member(member)
                .name(childRequest.getName())
                .englishAge(childRequest.getEnglishAge())
                .koreanAge(childRequest.getKoreanAge())
                .build());

        return childRepository.findByMemberId(memberId)
                .stream()
                .map(MemberDto.ChildDetail::new)
                .toList();
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
     * 자녀 삭제
     */
    @Transactional
    public void deleteChild(Long memberId, Long childId) {
        Child child = childRepository.findById(childId).orElseThrow(
                () -> new MemberException(MemberErrorCode.CHILD_NOT_FOUND)
        );
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        if(!member.getChildren().contains(child)) {
            throw new MemberException(MemberErrorCode.CANNOT_ACCESS_CHILD);
        }
        member.removeChild(child);
        childRepository.delete(child);
    }

    /**
     * 자녀 정보 수정
     */
    @Transactional
    public List<MemberDto.ChildDetail> updateChild(Long memberId, Long childId, MemberDto.ChildRequest childRequest) {
        Child child = childRepository.findById(childId).orElseThrow(
                () -> new MemberException(MemberErrorCode.CHILD_NOT_FOUND)
        );
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        if(!member.getChildren().contains(child)) {
            throw new MemberException(MemberErrorCode.CANNOT_ACCESS_CHILD);
        }

        // 나이 검사
        if (childRequest.getEnglishAge() < 3 || childRequest.getEnglishAge() > 8 ||
                childRequest.getKoreanAge() < 3 || childRequest.getKoreanAge() > 8) {
            throw new MemberException(MemberErrorCode.INVALID_AGE);
        }

        child.updateChild(childRequest.getName(), childRequest.getEnglishAge(), childRequest.getKoreanAge());
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
    public List<MemberDto.MemberSummary> getFollowers(Long memberId) {
        List<MemberDto.MemberSummary> followers = new ArrayList<>();
        followRepository.findByFollowingId(memberId).forEach(
                following -> {
                    followers.add(MemberDto.MemberSummary.builder()
                                    .member(following.getFollower())
                            .build());
                }
        );
        return followers;
    }
}
