package com.example.matdongsanserver.domain.member.controller;


import com.example.matdongsanserver.domain.auth.util.SecurityUtils;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Member API", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입 이후 닉네임 및 프로필 이미지 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberDto.MemberDetail> updateMember(
            @RequestParam String nickname,
            @RequestPart("multipartFile") MultipartFile profileImage
    ) {
        return ResponseEntity.ok()
                .body(memberService.updateMember(SecurityUtils.getLoggedInMemberId(), nickname, profileImage));
    }

    @Operation(summary = "회원 조회")
    @GetMapping
    public ResponseEntity<MemberDto.MemberDetail> getMemberDetail(
    ) {
        return ResponseEntity.ok()
                .body(memberService.getMemberDetail(SecurityUtils.getLoggedInMemberId()));
    }

    @Operation(summary = "자녀 추가")
    @PostMapping("/children")
    public ResponseEntity<List<MemberDto.ChildDetail>> registerChild(
            @RequestBody MemberDto.ChildCreationRequest childCreationRequest
    ) {
        return ResponseEntity.ok()
                .body(memberService.registerChild(SecurityUtils.getLoggedInMemberId(), childCreationRequest));
    }

    @Operation(summary = "자녀 조회")
    @GetMapping("/children")
    public ResponseEntity<List<MemberDto.ChildDetail>> getChildDetails(
    ) {
        return ResponseEntity.ok()
                .body(memberService.getChildDetails(SecurityUtils.getLoggedInMemberId()));
    }

    @Operation(summary = "팔로우")
    @PostMapping("/follow/{followerId}")
    public ResponseEntity<Void> follow(
            @PathVariable Long followerId
    ) {
        memberService.follow(SecurityUtils.getLoggedInMemberId(), followerId);
        return ResponseEntity.noContent()
                .build();
    }

    @Operation(summary = "언팔로우")
    @DeleteMapping("/follow/{followerId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long followerId
    ) {
        memberService.unfollow(SecurityUtils.getLoggedInMemberId(), followerId);
        return ResponseEntity.noContent()
                .build();
    }

    @Operation(summary = "팔로우 리스트 조회")
    @GetMapping("/follow")
    public ResponseEntity<List<MemberDto.MemberSummary>> getFollowers(
    ) {
        return ResponseEntity.ok()
                .body(memberService.getFollowers(SecurityUtils.getLoggedInMemberId()));
    }
}
