package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.common.config.ChatGptConfig;
import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.domain.story.document.Language;
import com.example.matdongsanserver.domain.story.document.Story;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.StoryRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final PromptsConfig promptsConfig;

    private final ChatGptConfig chatGptConfig;

    private final StoryRepository storyRepository;

    /**
     * 동화 생성
     */
    @Transactional
    public StoryDto.StoryCreationResponse generateStory(StoryDto.StoryCreationRequest requestDto) throws IOException {
        String prompt = getPromptForAge(requestDto.getAge(), requestDto.getLanguage(), requestDto.getTheme());
        int maxTokens = getMaxTokensForAge(requestDto.getAge());

        return StoryDto.StoryCreationResponse.builder()
                .story(storyRepository.save(Story.builder()
                        .age(requestDto.getAge())
                        .language(requestDto.getLanguage())
                        .theme(requestDto.getTheme())
                        .title("제목 미정") //제목 로직 추후 수정 필요
                        .content(sendOpenAiRequest(prompt, maxTokens))
                        .coverUrl("https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9788934935018.jpg") //이미지 로직 추후 수정 필요
                        .build()))
                .build();
    }

    /**
     * 동화 상세 수정
     */
    @Transactional
    public StoryDto.StoryDetail updateStoryDetail(String id, StoryDto.StoryUpdateRequest requestDto) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND))
                .updateStoryDetail(requestDto);

        return StoryDto.StoryDetail.builder()
                .story(storyRepository.save(story))
                .build();
    }

    /**
     * 전체 동화 리스트 조회
     */
    public List<StoryDto.StorySummary> getAllStories() {
        return storyRepository.findAll()
                .stream()
                .map(StoryDto.StorySummary::new)
                .toList();
    }

    /**
     * 동화 상세 조회
     */
    public StoryDto.StoryDetail getStoryDetail(String id) {
        return StoryDto.StoryDetail.builder()
                .story(storyRepository.findById(id)
                        .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)))
                .build();
    }

    /**
     * 입력 받은 테마와 나이, 언어를 통해 프롬프트 제공
     */
    private String getPromptForAge(int age, Language language, String theme) {
        if (language == Language.EN) {
            String template = promptsConfig.getEn().get(age);
            if (template != null) {
                return String.format(template, theme);
            }
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        } else if (language == Language.KO) {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE); //한국어 추후 처리 필요
        } else {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE);
        }
    }

    /**
     * 나이별 최대 토큰 설정 (전체 글 길이 조절)
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

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.9);
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "Generate text that faithfully fulfills the user's request."),
                Map.of("role", "user", "content", prompt)
        });

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = chatGptConfig.restTemplate().exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            StoryDto.ChatGptResponse chatGptResponse = objectMapper.readValue(response.getBody(), new TypeReference<StoryDto.ChatGptResponse>(){});
            return chatGptResponse.getChoices().get(0).getMessage().getContent();
        } else {
            throw new StoryException(StoryErrorCode.STORY_GENERATION_FAILED);
        }
    }
}
