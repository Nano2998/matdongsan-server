package com.example.matdongsanserver.domain.follow.service;

import com.example.matdongsanserver.domain.follow.entity.Follow;
import com.example.matdongsanserver.domain.follow.exception.FollowErrorCode;
import com.example.matdongsanserver.domain.follow.exception.FollowException;
import com.example.matdongsanserver.domain.follow.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
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
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    /**
     * 팔로우
     */
    @Transactional
    public void follow(Long memberId, Long followerId) {
        if (followRepository.findByFollowingIdAndFollowerId(memberId, followerId).isPresent()) {
            throw new FollowException(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
        }
        followRepository.save(Follow.builder()
                .following(memberRepository.findByIdOrThrow(memberId))
                .follower(memberRepository.findByIdOrThrow(followerId))
                .build());
    }

    /**
     * 팔로우 취소
     */
    @Transactional
    public void unfollow(Long memberId, Long followerId) {
        Follow follow = followRepository.findByFollowingIdAndFollowerId(memberId, followerId).orElseThrow(
                () ->  new FollowException(FollowErrorCode.FOLLOW_NOT_EXISTS)
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
