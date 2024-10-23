package com.example.matdongsanserver;

import com.example.matdongsanserver.common.config.PromptsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PromptsConfig.class)
public class MatdongsanServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatdongsanServerApplication.class, args);
    }

}
