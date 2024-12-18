package com.example.matdongsanserver.domain.child.controller;

import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.service.ChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Child API", description = "자녀 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/children")
public class ChildController {

    private final ChildService childService;

    @Operation(summary = "자녀 추가")
    @PostMapping
    public ResponseEntity<List<ChildDto.ChildDetail>> registerChild(
            @RequestBody ChildDto.ChildRequest childRequest
    ) {
        return ResponseEntity.ok()
                .body(childService.registerChild(SecurityUtils.getLoggedInMemberId(), childRequest));
    }

    @Operation(summary = "자녀 조회")
    @GetMapping
    public ResponseEntity<List<ChildDto.ChildDetail>> getChildDetails(
    ) {
        return ResponseEntity.ok()
                .body(childService.getChildDetails(SecurityUtils.getLoggedInMemberId()));
    }

    @Operation(summary = "자녀 정보 수정")
    @PatchMapping("/{childId}")
    public ResponseEntity<List<ChildDto.ChildDetail>> updateChild(
            @PathVariable Long childId,
            @RequestBody ChildDto.ChildRequest childRequest
    ) {
        return ResponseEntity.ok()
                .body(childService.updateChild(SecurityUtils.getLoggedInMemberId(), childId, childRequest));
    }

    @Operation(summary = "자녀 삭제")
    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(
            @PathVariable Long childId
    ) {
        childService.deleteChild(SecurityUtils.getLoggedInMemberId(), childId);
        return ResponseEntity.noContent()
                .build();
    }
}
