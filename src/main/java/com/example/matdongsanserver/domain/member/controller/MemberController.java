package com.example.matdongsanserver.domain.member.controller;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member API", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 생성")
    @PostMapping
    public ResponseEntity<MemberDto.MemberDetail> registerMember(
            @RequestBody MemberDto.MemberCreationRequest memberCreationRequest
    ) {
        return ResponseEntity.ok()
                .body(memberService.registerMember(memberCreationRequest));
    }

    @Operation(summary = "회원 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto.MemberDetail> getMemberDetail(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(memberService.getMemberDetail(memberId));
    }

    @Operation(summary = "자녀 생성")
    @PostMapping("/children/{memberId}")
    public ResponseEntity<List<MemberDto.ChildDetail>> registerChild(
            @RequestBody List<MemberDto.ChildCreationRequest> childCreationRequests,
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(memberService.registerChild(memberId, childCreationRequests));
    }

    @Operation(summary = "자녀 조회")
    @GetMapping("/children/{memberId}")
    public ResponseEntity<List<MemberDto.ChildDetail>> registerChild(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok()
                .body(memberService.getChildDetails(memberId));
    }
}
