package com.example.matdongsanserver.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "맛동산(맞춤형 동화 산타) API 명세서",
                description = "맛동산(맞춤형 동화 산타) API 명세서입니다.",
                version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi chatOpenApi() {
        return GroupedOpenApi.builder()
                .group("밥4조")
                .pathsToMatch("/**")
                .build();
    }
}
