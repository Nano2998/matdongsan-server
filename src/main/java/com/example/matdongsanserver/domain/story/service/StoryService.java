package com.example.matdongsanserver.domain.story.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.client.OpenAiClient;
import com.example.matdongsanserver.domain.story.client.TTSClient;
import com.example.matdongsanserver.domain.story.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final OpenAiClient openAIClient;
    private final TTSClient ttsClient;
    private final PromptsConfig promptsConfig;
    private final AmazonS3 amazonS3;
    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final StoryQuestionRepository storyQuestionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final LibraryService libraryService;

    /**
     * 동화 생성
     *
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public StoryDto.StoryCreationResponse createStory(Long memberId, StoryDto.StoryCreationRequest requestDto) {
        Language language = Language.fromString(requestDto.getLanguage());
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        // 프롬프트와 토큰 설정
        String prompt = getPromptForAge(requestDto.getAge(), language, requestDto.getGiven());
        int maxTokens = determineMaxTokens(language, requestDto.getAge());

        // 동화 생성 요청 및 응답 파싱
        String responseContent = sendStoryCreationRequest(prompt, maxTokens, language);
        Map<String, String> storyDetails = parseStoryResponse(responseContent);

        Story save = storyRepository.save(Story.builder()
                .age(requestDto.getAge())
                .language(language)
                .given(requestDto.getGiven())
                .title(storyDetails.get("title"))
                .content(storyDetails.get("content"))
                .memberId(memberId)
                .author(member.getNickname())
                .coverUrl("")
                .build());

        // 동화 요약 및 커버 이미지 생성 요청
        String summary = sendSummaryRequest(storyDetails.get("content"));
        save.updateCoverUrl(sendImageRequest(save.getId(), summary));
        storyRepository.save(save);

        // 생성된 동화를 최근 동화에 포함
        libraryService.addRecentStories(memberId,save.getId());

        return StoryDto.StoryCreationResponse.builder()
                .story(save)
                .build();
    }

    /**
     * 입력 받은 테마와 나이, 언어를 통해 프롬프트 제공
     *
     * @param age
     * @param language
     * @param given
     * @return
     */
    private String getPromptForAge(int age, Language language, String given) {
        Map<Integer, String> templates = switch (language) {
            case EN -> promptsConfig.getEn();
            case KO -> promptsConfig.getKo();
        };

        String template = templates.get(age);
        if (template == null) {
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        }

        return template.replace("{given}", given);
    }

    /**
     * 언어별 나이별 최대 토큰 설정 (전체 동화 길이 결정)
     *
     * @param language
     * @param age
     * @return
     */
    private int determineMaxTokens(Language language, int age) {
        return switch (language) {
            case EN -> switch (age) {
                case 3, 4 -> 300;
                case 5, 6 -> 450;
                case 7 -> 700;
                case 8 -> 750;
                default -> throw new StoryException(StoryErrorCode.INVALID_AGE);
            };
            case KO -> switch (age) {
                case 3, 4, 5, 6, 7, 8 -> 1024;
                default -> throw new StoryException(StoryErrorCode.INVALID_AGE);
            };
        };
    }

    /**
     * 동화 생성 요청을 전송
     *
     * @param prompt
     * @param maxTokens
     * @param language
     * @return
     */
    private String sendStoryCreationRequest(String prompt, int maxTokens, Language language) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", language == Language.KO ? "chatgpt-4o-latest" : "gpt-4o-mini");
        requestBody.put("max_tokens", maxTokens);
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
            throw new StoryException(StoryErrorCode.STORY_GENERATION_FAILED);
        }
        return parseChatGptResponse(response.getBody());
    }

    /**
     * GPT의 응답을 받아서 필요한 content만 파싱
     *
     * @param responseBody
     * @return
     */
    private String parseChatGptResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new StoryException(StoryErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * 동화 제목, 내용 파싱
     *
     * @param response
     * @return
     */
    private Map<String, String> parseStoryResponse(String response) {
        Map<String, String> parsedStory = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(response);

            String title = rootNode.path("title").asText().trim();
            String content = rootNode.path("content").asText().trim();

            parsedStory.put("title", title);
            parsedStory.put("content", content);
        } catch (Exception e) {
            log.warn("Json parsing error: {}", response);
            throw new StoryException(StoryErrorCode.JSON_PARSING_ERROR);
        }

        return parsedStory;
    }

    /**
     * 동화 상세 수정
     *
     * @param memberId
     * @param storyId
     * @param requestDto
     * @return
     */
    @Transactional
    public StoryDto.StoryDetail updateStoryDetail(Long memberId, String storyId, StoryDto.StoryUpdateRequest requestDto) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND))
                .updateStoryDetail(requestDto);

        if (!story.getMemberId().equals(memberId)) {
            throw new StoryException(StoryErrorCode.STORY_EDIT_PERMISSION_DENIED);  // 동화의 주인만 동화 상세 수정 가능
        }

        return StoryDto.StoryDetail.builder()
                .story(storyRepository.save(story))
                .isLiked(storyLikeRepository.existsByStoryIdAndMemberId(storyId, memberId))
                .build();
    }

    /**
     * 동화 상세 조회
     *
     * @param storyId
     * @param memberId
     * @return
     */
    public StoryDto.StoryDetail getStoryDetail(String storyId, Long memberId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND));

        // 조회하는 동화를 최근 동화에 포함
        libraryService.addRecentStories(memberId, storyId);

        return StoryDto.StoryDetail.builder()
                .story(story)
                .isLiked(storyLikeRepository.existsByStoryIdAndMemberId(storyId, memberId))
                .build();
    }

    /**
     * 동화 좋아요
     *
     * @param storyId
     * @param memberId
     */
    @Transactional
    public void addLike(String storyId, Long memberId) {
        Story story = storyRepository.findById(storyId).orElseThrow(
                () -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)
        );

        if (storyLikeRepository.findByStoryIdAndMemberId(storyId, memberId).isPresent()) {
            throw new StoryException(StoryErrorCode.LIKE_ALREADY_EXISTS);   // 이미 좋아요를 누른 경우
        }

        storyLikeRepository.save(StoryLike.builder()
                .storyId(storyId)
                .member(memberRepository.findById(memberId).orElseThrow(
                        () -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                ))
                .build());

        storyRepository.save(story.addLikes());
    }

    /**
     * 동화 좋아요 취소
     *
     * @param storyId
     * @param memberId
     */
    @Transactional
    public void removeLike(String storyId, Long memberId) {
        Story story = storyRepository.findById(storyId).orElseThrow(
                () -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)
        );

        storyLikeRepository.delete(storyLikeRepository.findByStoryIdAndMemberId(storyId, memberId)
                .orElseThrow(
                        () -> new StoryException(StoryErrorCode.LIKE_NOT_EXISTS)    //좋아요를 누르지 않고 취소 시도
                ));

        storyRepository.save(story.removeLikes());
    }

    /**
     * 동화 TTS 반환 - TTS가 이미 있다면 그대로 전달, 없다면 TTS 생성 요청 후 전달
     *
     * @param storyId
     * @return
     */
    @Transactional
    public String findOrCreateStoryTTS(String storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_NOT_FOUND));

        // 이미 해당 동화의 TTS가 저장되어 있다면 반환
        if (!story.getTtsUrl().isBlank()){
            return story.getTtsUrl();
        }

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
            String ttsUrl = uploadTTSToS3("tts/", storyId, response.getBody());
            storyRepository.save(story.updateTTSUrl(ttsUrl));
            return ttsUrl;
        } else {
            throw new StoryException(StoryErrorCode.TTS_GENERATION_FAILED);
        }
    }

    /**
     * TTS 파일을 S3에 업로드
     *
     * @param folderName
     * @param storyId
     * @param ttsData
     * @return
     */
    private String uploadTTSToS3(String folderName, String storyId, byte[] ttsData) {
        String fileName = folderName + storyId + ".mp3";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("audio/mpeg");
        metadata.setContentLength(ttsData.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(ttsData)) {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        } catch (IOException e) {
            throw new StoryException(StoryErrorCode.TTS_GENERATION_FAILED);
        }

        log.info("MP3 File uploaded: {}", amazonS3.getUrl(bucketName, fileName).toString());
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    /**
     * 동화 질문 생성
     *
     * @param storyId
     * @return
     */
    @Transactional
    public StoryDto.StoryQuestionResponse generateQuestions(String storyId) {
        Story story = storyRepository.findById(storyId).orElseThrow(
                () -> new StoryException(StoryErrorCode.STORY_NOT_FOUND)
        );

        StoryQuestion storyQuestion = storyQuestionRepository.save(StoryQuestion.builder()
                .storyId(storyId)
                .language(story.getLanguage())
                .build());

        // 동화 질문 생성 요청 및 파싱 후 저장
        parseQuestion(sendQuestionRequest(story.getLanguage(), story.getAge(), story.getContent()), storyQuestion);

        return StoryDto.StoryQuestionResponse.builder()
                .storyquestion(storyQuestion)
                .build();
    }

    /**
     * 동화 질문 생성 요청 전송
     *
     * @param language
     * @param age
     * @param story
     * @return
     */
    private String sendQuestionRequest(Language language, int age, String story) {
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
            throw new StoryException(StoryErrorCode.QUESTION_GENERATION_FAILED);
        }
        return parseChatGptResponse(response.getBody());
    }

    /**
     * 동화 질문 파싱 후 저장
     *
     * @param input
     * @param storyQuestion
     */
    private void parseQuestion(String input, StoryQuestion storyQuestion) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> jsonMap = objectMapper.readValue(input, new TypeReference<Map<String, String>>() {});

            // Q와 A 키를 순회하며 매핑
            for (int i = 1; i < 4; i++) {
                QuestionAnswer pair = questionAnswerRepository.save(QuestionAnswer.builder()
                        .question(jsonMap.get("Q" + i).trim())
                        .sampleAnswer(jsonMap.get("A" + i).trim())
                        .storyQuestion(storyQuestion)
                        .build());
            }
        } catch (IOException e) {
            throw new StoryException(StoryErrorCode.JSON_PARSING_ERROR);
        }
    }


    /**
     * 동화 질문 TTS로 변환후 S3 업로드 후 링크 반환 - 응답이 오면 S3에서 해당 파일 삭제 필요
     *
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
            return uploadTTSToS3("tts_question/", String.valueOf(questionId), response.getBody());
        } else {
            throw new StoryException(StoryErrorCode.TTS_GENERATION_FAILED);
        }
    }

    /**
     * 동화 요약 요청을 전송
     *
     * @param content
     * @return
     */
    private String sendSummaryRequest(String content) {
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
            throw new StoryException(StoryErrorCode.STORY_SUMMARY_FAILED);
        }
        return parseChatGptResponse(response.getBody());
    }

    /**
     * 동화 이미지 생성 요청을 전송
     *
     * @param scene
     * @return
     */
    private String sendImageRequest(String scene, String storyId) {
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
            throw new StoryException(StoryErrorCode.STORY_IMAGE_GENERATION_FAILED);
        }
        String imageUrl = parseImageResponse(response.getBody());
        return uploadImageToS3("cover/", storyId, imageUrl);
    }

    /**
     * 이미지 응답 파싱
     *
     * @param responseBody
     * @return
     */
    private String parseImageResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                return dataNode.get(0).path("url").asText();
            } else {
                throw new StoryException(StoryErrorCode.STORY_IMAGE_GENERATION_FAILED);
            }
        } catch (JsonProcessingException e) {
            throw new StoryException(StoryErrorCode.STORY_IMAGE_GENERATION_FAILED);
        }
    }

    /**
     * 생성된 이미지 URL에서 이미지를 S3에 업로드
     *
     * @param folderName
     * @param storyId
     * @param imageUrl
     * @return
     */
    private String uploadImageToS3(String folderName, String storyId, String imageUrl) {
        String fileName = folderName + storyId + ".png";

        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            byte[] imageData = buffer.toByteArray();
            metadata.setContentLength(imageData.length);

            try (ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageData)) {
                amazonS3.putObject(new PutObjectRequest(bucketName, fileName, imageInputStream, metadata));
            }

            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new StoryException(StoryErrorCode.STORY_IMAGE_GENERATION_FAILED);
        }
    }

    /**
     * STT 요청 전송 및 응답을 저장
     * @param file
     */
    @Transactional
    public String sendSTTRequest(MultipartFile file) {
        ResponseEntity<String> response = openAIClient.sendSTTRequest(
                "Bearer " + apiKey,
                "whisper-1",
                file
        );
        Long questionId = Long.parseLong(Objects.requireNonNull(file.getOriginalFilename()).replace("-recorded.mp3", ""));
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(questionId).orElseThrow(
                () -> new StoryException(StoryErrorCode.INVALID_FILE_NAME)
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return questionAnswer.updateAnswer(parseSTTResponse(response.getBody()));
        } else {
            throw new StoryException(StoryErrorCode.STT_GENERATION_FAILED);
        }
    }

    /**
     * STT 응답 파싱
     *
     * @param response
     * @return
     */
    private String parseSTTResponse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("text").asText().trim();
        } catch (Exception e) {
            log.warn("Json parsing error: {}", response);
            throw new StoryException(StoryErrorCode.JSON_PARSING_ERROR);
        }
    }
}
