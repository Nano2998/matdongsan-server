package com.example.matdongsanserver.domain.story.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatGptResponse {
    private String id;
    private List<ChoiceResponse> choices;
}
