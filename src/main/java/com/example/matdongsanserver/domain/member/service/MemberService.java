package com.example.matdongsanserver.domain.member.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final FollowRepository followRepository;
    private final StoryRepository storyRepository;
    private final AmazonS3 amazonS3;

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
                .map(this::uploadFile)
                .orElse("https://i.namu.wiki/i/M0j6sykCciGaZJ8yW0CMumUigNAFS8Z-dJA9h_GKYSmqqYSQyqJq8D8xSg3qAz2htlsPQfyHZZMmAbPV-Ml9UA.webp"));

        return MemberDto.MemberDetail.builder()
                .member(findMember)
                .storyCount(storyRepository.countByMemberId(memberId))
                .build();
    }

    public String uploadFile(MultipartFile profileImage) {
        try {
            // 고유한 파일 이름 생성
            String fileName = generateFileName(profileImage.getOriginalFilename());

            // 파일 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(profileImage.getSize());
            metadata.setContentType(profileImage.getContentType());

            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, profileImage.getInputStream(), metadata));

            // 업로드된 파일의 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new MemberException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }

    private String generateFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new MemberException(MemberErrorCode.INVALID_FILE_NAME);
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return "profiles/" + UUID.randomUUID() + extension;
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
     * 자녀 생성
     */
    @Transactional
    public List<MemberDto.ChildDetail> registerChild(Long memberId, List<MemberDto.ChildCreationRequest> childCreationRequests) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        List<MemberDto.ChildDetail> childDetails = new ArrayList<>();

        childCreationRequests.forEach(childCreationRequest -> {
            if (childCreationRequest.getEnglishAge() < 3 || childCreationRequest.getEnglishAge() > 8 ||
                    childCreationRequest.getKoreanAge() < 3 || childCreationRequest.getKoreanAge() > 8) {
                throw new MemberException(MemberErrorCode.INVALID_AGE);
            }
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
