package com.example.matdongsanserver.domain.story.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.service.StoryService;
import com.nimbusds.jose.shaded.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(StoryController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryService storyService;

    @MockBean
    private TokenProvider tokenProvider;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    Gson gson = new Gson();

    @BeforeEach
    void setup() {
        // 시큐리티 관련 Mock
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getLoggedInMemberId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        // 시큐리티 관련 Mock 해제
        // 해제하지 않을 경우 충돌 발생
        mockedSecurityUtils.close();
    }

    @Test
    @DisplayName("동화 생성 성공")
    @WithMockUser
    void registerStory() throws Exception {
        // Given
        StoryDto.StoryCreationRequest requestDto = new StoryDto.StoryCreationRequest("KO", 5, "Once upon a time...");
        StoryDto.StoryCreationResponse responseDto = new StoryDto.StoryCreationResponse(
                "1", "New Story", "Content", "Author", "http://cover.url"
        );

        when(storyService.registerStory(eq(1L), any(StoryDto.StoryCreationRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/stories")
                        .contentType("application/json")
                        .content(gson.toJson(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("New Story"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.author").value("Author"));
    }

    @Test
    @DisplayName("동화 상세 수정 성공")
    @WithMockUser
    void updateStoryDetail() throws Exception {
        // Given
        String storyId = "1";
        StoryDto.StoryUpdateRequest requestDto = new StoryDto.StoryUpdateRequest(
                "Updated Title", true, List.of("tag1", "tag2")
        );
        StoryDto.StoryDetail responseDto = StoryDto.StoryDetail.builder()
                .story(createStory("Updated Title", 1L, List.of("tag1", "tag2"), true))
                .isMyStory(true)
                .isFollowed(false)
                .isLiked(false)
                .build();

        when(storyService.updateStoryDetail(eq(1L), eq(storyId), any(StoryDto.StoryUpdateRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/stories/{storyId}", storyId)
                        .contentType("application/json")
                        .content(gson.toJson(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.authorId").value(1L))
                .andExpect(jsonPath("$.tags[0]").value("tag1"))
                .andExpect(jsonPath("$.tags[1]").value("tag2"));
    }

    @Test
    @DisplayName("동화 상세 조회 성공")
    @WithMockUser
    void getStoryDetail() throws Exception {
        // Given
        String storyId = "1";
        StoryDto.StoryDetail responseDto = StoryDto.StoryDetail.builder()
                .story(createStory("Story Title", 1L, List.of("tag1", "tag2"), true))
                .isMyStory(true)
                .isFollowed(false)
                .isLiked(false)
                .build();

        when(storyService.getStoryDetail(eq(storyId), eq(1L))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/stories/{storyId}", storyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(1L))
                .andExpect(jsonPath("$.title").value("Story Title"))
                .andExpect(jsonPath("$.content").value("testContent"));
    }

    @Test
    @DisplayName("동화 TTS 성공")
    @WithMockUser
    void getStoryTTS() throws Exception {
        // Given
        String storyId = "1";
        StoryDto.TTSResponse responseDto = new StoryDto.TTSResponse("http://tts.url", List.of(0.0, 1.5, 3.2));

        when(storyService.getOrRegisterStoryTTS(eq(storyId))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/stories/tts/{storyId}", storyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ttsUrl").value("http://tts.url"))
                .andExpect(jsonPath("$.timestamps[1]").value(1.5));
    }

    @Test
    @DisplayName("동화 좋아요 성공")
    @WithMockUser
    void likeStory() throws Exception {
        // Given
        String storyId = "1";

        // When & Then
        mockMvc.perform(post("/api/stories/likes/{storyId}", storyId))
                .andExpect(status().isNoContent());
        verify(storyService, times(1)).likeStory(eq(storyId), eq(1L));
    }

    @Test
    @DisplayName("동화 좋아요 취소 성공")
    @WithMockUser
    void unlikeStory() throws Exception {
        // Given
        String storyId = "1";

        // When & Then
        mockMvc.perform(delete("/api/stories/likes/{storyId}", storyId))
                .andExpect(status().isNoContent());
        verify(storyService, times(1)).unlikeStory(eq(storyId), eq(1L));
    }

    private Story createStory(String title, Long memberId, List<String> tags, boolean isPublic) {
        Story story = Story.builder()
                .title(title)
                .age(3)
                .author("testAuthor")
                .content("testContent")
                .coverUrl("testCover")
                .memberId(memberId)
                .language(Language.KO)
                .given("testGiven")
                .build();

        if (tags != null || isPublic) {
            story.updateStoryDetail(StoryDto.StoryUpdateRequest.builder()
                    .tags(tags)
                    .isPublic(isPublic)
                    .title(title)
                    .build());
        }
        return story;
    }
}