package com.example.matdongsanserver.common.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "HealthCheck API", description = "헬스 체크 API")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "헬스 체크")
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        log.info("로그 체크");
        return ResponseEntity.ok()
                .body("healthy");
    }


}
