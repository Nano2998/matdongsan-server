package com.example.matdongsanserver.domain.story.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.matdongsanserver.common.config.ChatGptConfig;
import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
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
    private String aiModel;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.tts.model}")
    private String ttsModel;

    @Value("${openai.tts.voice}")
    private String ttsVoice;

    @Value("${openai.tts.url}")
    private String ttsUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final PromptsConfig promptsConfig;

    private final ChatGptConfig chatGptConfig;

    private final StoryRepository storyRepository;

    private final AmazonS3 amazonS3;

    private final MemberRepository memberRepository;

    /**
     * 동화 생성
     */
    @Transactional
    public StoryDto.StoryCreationResponse generateStory(Long memberId, StoryDto.StoryCreationRequest requestDto) throws IOException {
        memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );
        String prompt = getPromptForAge(requestDto.getAge(), requestDto.getLanguage(), requestDto.getGiven());
        int maxTokens = 0;
        if (requestDto.getLanguage() == Language.EN) {
            maxTokens = getMaxTokensForAgeEn(requestDto.getAge());
        } else if (requestDto.getLanguage() == Language.KO) {
            maxTokens = getMaxTokensForAgeKo(requestDto.getAge());
        }
        Map<String, String> parseStory = parseStoryResponse(sendOpenAiRequest(prompt, maxTokens));

        return StoryDto.StoryCreationResponse.builder()
                .story(storyRepository.save(Story.builder()
                        .age(requestDto.getAge())
                        .language(requestDto.getLanguage())
                        .given(requestDto.getGiven())
                        .title(parseStory.get("title"))
                        .content(parseStory.get("content"))
                        .memberId(memberId)
                        .coverUrl("https://contents.kyobobook.co.kr/sih/fit-in/458x0/pdt/9788934935018.jpg") //이미지 로직 추후 수정 필요
                        .build()))
                .build();
    }

    /**
     * 영어 동화 번역 - 번역이 이미 있다면 그대로 전달, 없다면 번역 요청 후 전달
     */
    @Transactional
    public StoryDto.StoryTranslationResponse translationStory(String storyId) throws IOException {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND));
        if (story.getLanguage().equals(Language.KO)) {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE_FOR_TRANSLATION);
        }
        if (story.getTranslationTitle().isBlank()) {
            Map<String, String> parseStory = parseStoryResponse(sendTranslationRequest(story.getTitle(), story.getContent()));
            story.updateTranslation(parseStory.get("title"), parseStory.get("content"));
            return StoryDto.StoryTranslationResponse.builder()
                    .story(storyRepository.save(story))
                    .build();
        } else{
            return StoryDto.StoryTranslationResponse.builder()
                    .story(storyRepository.save(story))
                    .build();
        }
    }

    /**
     * 동화 상세 수정
     */
    @Transactional
    public StoryDto.StoryDetail updateStoryDetail(Long memberId, String storyId, StoryDto.StoryUpdateRequest requestDto) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND))
                .updateStoryDetail(requestDto);

        if (!story.getMemberId().equals(memberId)) {
            throw new StoryException(StoryErrorCode.STORY_EDIT_PERMISSION_DENIED);
        }

        return StoryDto.StoryDetail.builder()
                .story(storyRepository.save(story))
                .build();
    }

    /**
     * 전체 동화 리스트 조회 (isPubic 체크 필요)
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
    public StoryDto.StoryDetail getStoryDetail(String storyId) {
        return StoryDto.StoryDetail.builder()
                .story(storyRepository.findById(storyId)
                        .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)))
                .build();
    }

    /**
     * tts 변환 -> 추후에 비동기 처리 고민
     */
    @Transactional
    public String getStoryTTS(String id) throws IOException {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND));

        // 현재는 영어 tts만 가능, 추후에 로직 추가 예정
        if(story.getLanguage() == Language.KO){
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE_FOR_TRANSLATION);
        }

        // 이미 ttsUrl이 저장되어 있다면 반환
        if (!story.getTtsUrl().isBlank()){
            return story.getTtsUrl();
        }

        HttpHeaders headers = chatGptConfig.httpHeaders();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ttsModel);
        requestBody.put("voice", ttsVoice);
        requestBody.put("input", story.getContent());
        requestBody.put("speed", 0.95);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<byte[]> response = chatGptConfig.restTemplate().exchange(ttsUrl, HttpMethod.POST, request, byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String fileName = id + ".mp3";

            ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBody());

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/mpeg");
            metadata.setContentLength(response.getBody().length);

            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            String ttsUrl = amazonS3.getUrl(bucketName, fileName).toString();

            storyRepository.save(story.updateTTSUrl(ttsUrl));

            //return new ByteArrayResource(response.getBody());
            return ttsUrl;
        } else {
            throw new StoryException(StoryErrorCode.TTS_GENERATION_FAILED);
        }
    }

    /**
     * 입력 받은 테마와 나이, 언어를 통해 프롬프트 제공
     */
    private String getPromptForAge(int age, Language language, String given) {
        if (language == Language.EN) {
            String template = promptsConfig.getEn().get(age);
            if (template != null) {
                return String.format(template, given);
            }
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        } else if (language == Language.KO) {
            String template = promptsConfig.getKo().get(age);
            String storyElementsTemplate = promptsConfig.getStoryElements();
            if (storyElementsTemplate != null && template != null) {
                //String storyElements = storyElementsTemplate.replace("{given}", given).replace("{age}", String.valueOf(age));
                //return template.replace("{story_elements}", storyElements);
                return template.replace("{story_elements}", given);
            }
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        } else {
            throw new StoryException(StoryErrorCode.INVALID_LANGUAGE);
        }
    }

    /**
     * 영어 나이별 최대 토큰 설정 (전체 글 길이 조절)
     */
    private int getMaxTokensForAgeEn(int age) {
        return switch (age) {
            case 3, 4 -> 250;
            case 5, 6 -> 400;
            case 7 -> 700;
            case 8 -> 750;
            default -> throw new StoryException(StoryErrorCode.INVALID_AGE);
        };
    }

    /**
     * 한글 나이별 최대 토큰 설정 (전체 글 길이 조절)
     */
    private int getMaxTokensForAgeKo(int age) {
        return switch (age) {
            case 3, 4, 5, 6, 7, 8 -> 1024;
            default -> throw new StoryException(StoryErrorCode.INVALID_AGE);
        };
    }

    /**
     * chat gpt로 요청 후 스토리만 반환
     */
    private String sendOpenAiRequest(String prompt, int maxTokens) throws IOException {
        HttpHeaders headers = chatGptConfig.httpHeaders();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
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

    /**
     * 동화 제목, 내용 파싱
     */
    private Map<String, String> parseStoryResponse(String response) {
        Map<String, String> parsedStory = new HashMap<>();

        String[] parts = response.split("Title: ", 2);
        if (parts.length > 1) {
            String[] titleAndContent = parts[1].split(", Content: ", 2);

            String title = titleAndContent[0].trim();
            String content = titleAndContent.length > 1 ? titleAndContent[1].trim() : "";

            parsedStory.put("title", title);
            parsedStory.put("content", content);
        }

        return parsedStory;
    }

    /**
     * 영어 동화 번역 요청 전송
     */
    private String sendTranslationRequest(String title, String content) throws IOException {
        HttpHeaders headers = chatGptConfig.httpHeaders();
        String story = "Title: " + title + ", Content: " + content;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", promptsConfig.getTranslation()),
                Map.of("role", "user", "content", story)
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
