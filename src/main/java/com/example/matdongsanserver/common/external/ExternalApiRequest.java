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
     * 동화 생성 요청을 전송
     * @param prompt
     * @param language
     * @return
     */
    public Map<String, String> sendStoryCreationRequest(String prompt, Language language) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", language == Language.KO ? "chatgpt-4o-latest" : "gpt-4o-mini");
        requestBody.put("max_tokens", 2048);
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("temperature", 0.9);
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", promptsConfig.getGenerateCommand()),
                Map.of("role", "user", "content", prompt)
        });

        ResponseEntity<String> response = openAIClient.sendChatRequest(
                "Bearer " + apiKey,
                "application/json",
                requestBody
        );

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
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "You are making a prompt for an image generation model which image will be used as children's book cover."),
                Map.of("role", "user", "content", template)
        });

        ResponseEntity<String> response = openAIClient.sendChatRequest(
                "Bearer " + apiKey,
                "application/json",
                requestBody
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STORY_SUMMARY_FAILED);
        }
        return responseParser.extractChatGptContent(response.getBody());
    }

    /**
     * 동화 이미지 생성 요청을 전송
     * @param scene
     * @return
     */
    public String sendImageRequest(String storyId, String scene) {
        String template = promptsConfig.getGenerateImage().replace("{scene}", scene);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", template);
        requestBody.put("quality", "standard");
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");

        ResponseEntity<String> response = openAIClient.sendImageRequest(
                "Bearer " + apiKey,
                "application/json",
                requestBody
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.STORY_IMAGE_GENERATION_FAILED);
        }
        String imageUrl = responseParser.extractImageUrl(response.getBody());
        return s3Utils.uploadImageFromUrl("cover/", storyId, imageUrl);
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

        ResponseEntity<byte[]> response = ttsClient.sendTTSRequest(
                "application/json",
                ttsCreationRequest
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // 생성된 TTS를 S3에 업로드
            return s3Utils.uploadTTSToS3("tts/", storyId, response.getBody());
        } else {
            throw new BusinessException(CommonErrorCode.TTS_GENERATION_FAILED);
        }
    }

    /**
     * STT 요청 전송 및 응답을 반환
     * @param file
     */
    public String sendSTTRequest(MultipartFile file) {
        ResponseEntity<String> response = openAIClient.sendSTTRequest(
                "Bearer " + apiKey,
                "whisper-1",
                file
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return responseParser.extractSttText(response.getBody());
        } else {
            throw new BusinessException(CommonErrorCode.STT_GENERATION_FAILED);
        }
    }

    /**
     * 동화 질문 생성 요청 전송
     * @param language
     * @param age
     * @param story
     * @return
     */
    public List<Map<String, String>> sendQuestionRequest(Language language, int age, String story) {
        String template = promptsConfig.getQuestion();
        template = template.replace("{age}", Integer.toString(age));
        switch (language) {
            case KO -> template = template.replace("{language}", "korean");
            case EN -> template = template.replace("{language}", "english");
        }

        // 동화 질문 생성 요청 전송
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", template),
                Map.of("role", "user", "content", story)
        });

        ResponseEntity<String> response = openAIClient.sendChatRequest(
                "Bearer " + apiKey,
                "application/json",
                requestBody
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(CommonErrorCode.QUESTION_GENERATION_FAILED);
        }
        return responseParser.extractQuestions(responseParser.extractChatGptContent(response.getBody()));
    }

    /**
     * 동화 질문 TTS로 변환후 S3 업로드 후 링크 반환 - 응답이 오면 S3에서 해당 파일 삭제 필요
     * @param questionId
     * @param question
     * @param language
     * @return
     */
    public String getQuestionTTS(Long questionId, String question, Language language) {
        // TTS 생성 요청 전송
        StoryDto.TTSCreationRequest ttsCreationRequest = StoryDto.TTSCreationRequest.builder()
                .text(question)
                .file_name(String.valueOf(questionId))
                .language(language == Language.EN ? "EN" : "KR")
                .build();

        ResponseEntity<byte[]> response = ttsClient.sendTTSRequest(
                "application/json",
                ttsCreationRequest
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // 생성된 TTS를 S3에 업로드
            return s3Utils.uploadTTSToS3("tts_question/", String.valueOf(questionId), response.getBody());
        } else {
            throw new BusinessException(CommonErrorCode.TTS_GENERATION_FAILED);
        }
    }
}
