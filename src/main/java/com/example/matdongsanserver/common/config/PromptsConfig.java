package com.example.matdongsanserver.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "prompts")
public class PromptsConfig {
    private Map<Integer, String> en;
    private Map<Integer, String> ko;
    private String storyElements;
}
