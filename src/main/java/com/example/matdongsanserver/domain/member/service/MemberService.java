package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.common.utils.S3Utils;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final S3Utils s3Utils;
    private final MemberRepository memberRepository;
    private final StoryRepository storyRepository;

    /**
     * 회원가입 이후 닉네임 및 프로필 이미지 등록 로직
     */
    @Transactional
    public MemberDto.MemberDetail updateMember(Long memberId, String nickname, MultipartFile profileImage) {
        Member member = memberRepository.findByIdOrThrow(memberId);
        member.updateNickname(nickname);

        member.updateProfileImage(Optional.ofNullable(profileImage)
                .filter(file -> !file.isEmpty())
                .map(file -> s3Utils.uploadFile("profiles/", String.valueOf(memberId), file))
                .orElse(null));

        return MemberDto.MemberDetail.builder()
                .member(member)
                .storyCount(storyRepository.countByMemberId(memberId))
                .build();
    }

    /**
     * 맴버 조회
     */
    public MemberDto.MemberDetail getMemberDetail(Long memberId) {
        return MemberDto.MemberDetail.builder()
                .member(memberRepository.findByIdOrThrow(memberId))
                .storyCount(storyRepository.countByMemberId(memberId))
                .build();
    }
}
