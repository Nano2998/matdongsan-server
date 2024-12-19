package com.example.matdongsanserver.common.external;

import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.common.exception.BusinessException;
import com.example.matdongsanserver.common.exception.CommonErrorCode;
import com.example.matdongsanserver.common.utils.ResponseParser;
import com.example.matdongsanserver.common.utils.S3Utils;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiRequest {

    private final OpenAiClient openAIClient;
    private final TTSClient ttsClient;
    private final S3Utils s3Utils;
    private final PromptsConfig promptsConfig;
    private final ResponseParser responseParser;

    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * 공통 요청 생성 메서드
     * @param model
     * @param messages
     * @param additionalParams
     * @return
     */
    private Map<String, Object> createRequestBody(String model, Object[] messages, Map<String, Object> additionalParams) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        if (additionalParams != null) {
            requestBody.putAll(additionalParams);
        }
        return requestBody;
    }

    /**
     * 동화 생성 요청을 전송
     * @param prompt
     * @param language
     * @return
     */
    public Map<String, String> sendStoryCreationRequest(String prompt, Language language) {
        String model = language == Language.KO ? "chatgpt-4o-latest" : "gpt-4o-mini";
        Object[] messages = {
                Map.of("role", "system", "content", promptsConfig.getGenerateCommand()),
                Map.of("role", "user", "content", prompt)
        };

        Map<String, Object> requestBody = createRequestBody(model, messages, Map.of(
                "max_tokens", 2048,
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.9
        ));

        ResponseEntity<String> response = openAIClient.sendChatRequest("Bearer " + apiKey, "application/json", requestBody);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STORY_GENERATION_FAILED);
        }
        return responseParser.extractStoryDetails(responseParser.extractChatGptContent(response.getBody()));
    }

    /**
     * 동화 요약 요청을 전송
     * @param content
     * @return
     */
    public String sendSummaryRequest(String content) {
        String template = promptsConfig.getGenerateSummary().replace("{story}", content);
        Object[] messages = {
                Map.of("role", "system", "content", "You are making a prompt for an image generation model which image will be used as children's book cover."),
                Map.of("role", "user", "content", template)
        };

        Map<String, Object> requestBody = createRequestBody("gpt-4o-mini", messages, Map.of(
                "temperature", 0.7
        ));

        ResponseEntity<String> response = openAIClient.sendChatRequest("Bearer " + apiKey, "application/json", requestBody);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STORY_SUMMARY_FAILED);
        }
        return responseParser.extractChatGptContent(response.getBody());
    }

    /**
     * 동화 이미지 생성 요청을 전송
     * @param storyId
     * @param scene
     * @return
     */
    public String sendImageRequest(String storyId, String scene) {
        String template = promptsConfig.getGenerateImage().replace("{scene}", scene);

        Map<String, Object> requestBody = createRequestBody("dall-e-3", null, Map.of(
                "prompt", template,
                "quality", "standard",
                "n", 1,
                "size", "1024x1024"
        ));

        ResponseEntity<String> response = openAIClient.sendImageRequest("Bearer " + apiKey, "application/json", requestBody);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STORY_IMAGE_GENERATION_FAILED);
        }

        String imageUrl = responseParser.extractImageUrl(response.getBody());
        return s3Utils.uploadImageFromUrl("cover/", storyId, imageUrl);
    }

    /**
     * 동화 질문 생성 요청 전송
     * @param language
     * @param age
     * @param story
     * @return
     */
    public List<Map<String, String>> sendQuestionRequest(Language language, int age, String story) {
        String template = promptsConfig.getQuestion()
                .replace("{age}", Integer.toString(age))
                .replace("{language}", language == Language.KO ? "korean" : "english");

        Object[] messages = {
                Map.of("role", "system", "content", template),
                Map.of("role", "user", "content", story)
        };

        Map<String, Object> requestBody = createRequestBody("gpt-4o-mini", messages, Map.of(
                "response_format", Map.of("type", "json_object")
        ));

        ResponseEntity<String> response = openAIClient.sendChatRequest("Bearer " + apiKey, "application/json", requestBody);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.QUESTION_GENERATION_FAILED);
        }
        return responseParser.extractQuestions(responseParser.extractChatGptContent(response.getBody()));
    }

    /**
     * TTS 요청 전송 및 응답을 반환
     * @param storyId
     * @param story
     * @return
     */
    public String sendTTSRequest(String storyId, Story story) {
        StoryDto.TTSCreationRequest ttsCreationRequest = StoryDto.TTSCreationRequest.builder()
                .file_name(storyId)
                .text(story.getContent())
                .language(story.getLanguage() == Language.EN ? "EN" : "KR")
                .build();

        ResponseEntity<byte[]> response = ttsClient.sendTTSRequest("application/json", ttsCreationRequest);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(CommonErrorCode.TTS_GENERATION_FAILED);
        }

        return s3Utils.uploadTTSToS3("tts/", storyId, response.getBody());
    }

    /**
     * STT 요청 전송 및 응답을 반환
     * @param file
     * @return
     */
    public String sendSTTRequest(MultipartFile file) {
        ResponseEntity<String> response = openAIClient.sendSTTRequest("Bearer " + apiKey, "whisper-1", file);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STT_GENERATION_FAILED);
        }

        return responseParser.extractSttText(response.getBody());
    }

    /**
     * 동화 질문 TTS로 변환 후 S3 업로드 후 링크 반환
     * @param questionId
     * @param question
     * @param language
     * @return
     */
    public String getQuestionTTS(Long questionId, String question, Language language) {
        StoryDto.TTSCreationRequest ttsCreationRequest = StoryDto.TTSCreationRequest.builder()
                .text(question)
                .file_name(String.valueOf(questionId))
                .language(language == Language.EN ? "EN" : "KR")
                .build();

        ResponseEntity<byte[]> response = ttsClient.sendTTSRequest("application/json", ttsCreationRequest);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(CommonErrorCode.TTS_GENERATION_FAILED);
        }

        return s3Utils.uploadTTSToS3("tts_question/", String.valueOf(questionId), response.getBody());
    }
}