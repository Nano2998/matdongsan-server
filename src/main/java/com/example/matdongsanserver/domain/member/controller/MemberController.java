package com.example.matdongsanserver.domain.member.controller;

import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "내 정보 조회")
    @GetMapping
    public ResponseEntity<MemberDto.MemberDetail> getMemberDetail(
    ) {
        return ResponseEntity.ok()
                .body(memberService.getMemberDetail(SecurityUtils.getLoggedInMemberId()));
    }

    @Operation(summary = "다른 작가의 정보 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto.MemberDetailOther> getOtherMemberDetail(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(memberService.getOtherMemberDetail(SecurityUtils.getLoggedInMemberId(), memberId));
    }
}