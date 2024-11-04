package com.example.matdongsanserver.domain.member.controller;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member API", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 생성 - 로그인 도입 후 수정 예정")
    @PostMapping
    public ResponseEntity<MemberDto.MemberDetail> registerMember(
            @RequestBody MemberDto.MemberCreationRequest memberCreationRequest
    ) {
        return ResponseEntity.ok()
                .body(memberService.registerMember(memberCreationRequest));
    }

    
}
