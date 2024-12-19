package com.example.matdongsanserver.common.utils;

import com.example.matdongsanserver.common.exception.BusinessException;
import com.example.matdongsanserver.common.exception.CommonErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 문자열에서 특정 경로의 값을 추출하는 공통 메서드
     * @param json JSON 문자열
     * @param pathExtractor 경로 추출 로직
     * @param <T> 반환 타입
     * @return 추출된 값
     */
    private <T> T parseJson(String json, JsonNodeExtractor<T> pathExtractor) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return pathExtractor.extract(rootNode);
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * GPT의 응답에서 content 값 추출
     * @param responseBody GPT 응답 JSON 문자열
     * @return content 값
     */
    public String extractChatGptContent(String responseBody) {
        return parseJson(responseBody, rootNode ->
                rootNode.path("choices").get(0).path("message").path("content").asText()
        );
    }

    /**
     * 동화 제목과 내용을 추출
     * @param response 동화 응답 JSON 문자열
     * @return 제목과 내용이 포함된 Map
     */
    public Map<String, String> extractStoryDetails(String response) {
        return parseJson(response, rootNode -> Map.of(
                "title", rootNode.path("title").asText().trim(),
                "content", rootNode.path("content").asText().trim()
        ));
    }

    /**
     * 이미지 생성 응답에서 URL 추출
     * @param responseBody 이미지 생성 응답 JSON 문자열
     * @return 이미지 URL
     */
    public String extractImageUrl(String responseBody) {
        return parseJson(responseBody, rootNode -> {
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                return dataNode.get(0).path("url").asText();
            } else {
                throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
            }
        });
    }

    /**
     * STT 응답에서 텍스트 추출
     * @param response STT 응답 JSON 문자열
     * @return 텍스트
     */
    public String extractSttText(String response) {
        return parseJson(response, rootNode -> rootNode.path("text").asText().trim());
    }

    /**
     * 동화 질문과 답변을 파싱하여 리스트로 반환
     * @param input 질문 응답 JSON 문자열
     * @return 질문과 답변 리스트
     */
    public List<Map<String, String>> extractQuestions(String input) {
        try {
            return objectMapper.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * JSON 경로 추출을 위한 Functional Interface
     * @param <T> 반환 타입
     */
    @FunctionalInterface
    private interface JsonNodeExtractor<T> {
        T extract(JsonNode rootNode);
    }
}