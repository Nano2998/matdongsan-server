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
     * GPT의 응답에서 content 값 추출
     * @param responseBody GPT 응답 JSON 문자열
     * @return content 값
     */
    public String extractChatGptContent(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * 동화 제목과 내용을 추출
     * @param response 동화 응답 JSON 문자열
     * @return 제목과 내용이 포함된 Map
     */
    public Map<String, String> extractStoryDetails(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            String title = rootNode.path("title").asText().trim();
            String content = rootNode.path("content").asText().trim();
            return Map.of("title", title, "content", content);
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * 이미지 생성 응답에서 URL 추출
     * @param responseBody 이미지 생성 응답 JSON 문자열
     * @return 이미지 URL
     */
    public String extractImageUrl(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                return dataNode.get(0).path("url").asText();
            } else {
                throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
            }
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * STT 응답에서 텍스트 추출
     * @param response STT 응답 JSON 문자열
     * @return 텍스트
     */
    public String extractSttText(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("text").asText().trim();
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }

    /**
     * 동화 질문과 답변을 파싱하여 리스트로 반환
     * @param input 질문 응답 JSON 문자열
     * @return 질문과 답변 리스트
     */
    public List<Map<String, String>> extractQuestions(String input) {
        try {
            JsonNode rootNode = objectMapper.readTree(input);
            JsonNode questionsNode = rootNode.path("questions");

            return objectMapper.convertValue(
                    questionsNode,
                    new TypeReference<List<Map<String, String>>>() {}
            );
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_PARSING_ERROR);
        }
    }
}