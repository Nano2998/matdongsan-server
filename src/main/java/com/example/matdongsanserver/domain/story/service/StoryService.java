package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.common.config.ChatGptConfig;
import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.domain.story.dto.request.StoryRequestDto;
import com.example.matdongsanserver.domain.story.dto.response.ChatGptResponseDto;
import com.example.matdongsanserver.domain.story.dto.response.StoryResponseDto;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final PromptsConfig promptsConfig;

    private final ChatGptConfig chatGptConfig;

    /**
     * 동화 생성
     */
    public StoryResponseDto generateStory(StoryRequestDto requestDto) throws IOException {
        String prompt = getPromptForAge(requestDto.getAge(), requestDto.getLanguage(), requestDto.getTheme());
        int maxTokens = getMaxTokensForAge(requestDto.getAge());

        return StoryResponseDto.builder()
                .story(sendOpenAiRequest(prompt, maxTokens))
                .build();
    }

    /**
     * 입력 받은 테마와 나이, 언어를 통해 프롬프트 제공
     */
    private String getPromptForAge(int age, String language, String theme) {
        if (language.equals("en")) {
            String template = promptsConfig.getEn().get(age);
            if (template != null) {
                return String.format(template, theme);
            }
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        }
        throw new StoryException(StoryErrorCode.INVALID_LANGUAGE);
    }

    /**
     * 나이별 최대 토큰 설정
     */
    private int getMaxTokensForAge(int age) {
        return switch (age) {
            case 3, 4 -> 250;
            case 5, 6 -> 400;
            case 7 -> 700;
            case 8 -> 750;
            default -> throw new StoryException(StoryErrorCode.INVALID_AGE);
        };
    }

    /**
     * chat gpt로 요청 후 스토리만 반환
     */
    private String sendOpenAiRequest(String prompt, int maxTokens) throws IOException {
        HttpHeaders headers = chatGptConfig.httpHeaders();

        // 요청 본문 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.9);
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "Generate text that faithfully fulfills the user's request."),
                Map.of("role", "user", "content", prompt)
        });

        // 요청 본문을 JSON으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 요청 엔티티 생성
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        // API 요청 보내기
        ResponseEntity<String> response = chatGptConfig.restTemplate().exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ChatGptResponseDto chatGptResponseDto = objectMapper.readValue(response.getBody(), new TypeReference<ChatGptResponseDto>(){});
            return chatGptResponseDto.getChoices().get(0).getMessage().getContent();
        } else {
            throw new StoryException(StoryErrorCode.STORY_GENERATION_FAILED);
        }
    }
}
