package com.example.matdongsanserver.domain.dashboard.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.dashboard.dto.DashboardDto;
import com.example.matdongsanserver.domain.dashboard.service.DashboardService;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(DashboardController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

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
    @DisplayName("동화 질문 생성 및 반환 성공")
    @WithMockUser
    void registerQuestions() throws Exception {
        // Given
        String storyId = "story123";
        DashboardDto.StoryQuestionResponse response = new DashboardDto.StoryQuestionResponse(
                1L,
                storyId,
                Language.KO,
                List.of(
                        new DashboardDto.QnAs(1L, "Q1", "SA1", "A1"),
                        new DashboardDto.QnAs(2L, "Q2", "SA2", "A2")
                )
        );
        when(dashboardService.registerQuestions(storyId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/dashboard/questions/{storyId}", storyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storyId").value("story123"))
                .andExpect(jsonPath("$.language").value(Language.KO.toString()))
                .andExpect(jsonPath("$.qnAs[0].id").value(1L))
                .andExpect(jsonPath("$.qnAs[0].question").value("Q1"))
                .andExpect(jsonPath("$.qnAs[0].sampleAnswer").value("SA1"))
                .andExpect(jsonPath("$.qnAs[0].answer").value("A1"))
                .andExpect(jsonPath("$.qnAs[1].id").value(2L))
                .andExpect(jsonPath("$.qnAs[1].question").value("Q2"))
                .andExpect(jsonPath("$.qnAs[1].sampleAnswer").value("SA2"))
                .andExpect(jsonPath("$.qnAs[1].answer").value("A2"));
    }

    @Test
    @DisplayName("대시보드 QnA 모두보기 성공")
    @WithMockUser
    void getAllQna() throws Exception {
        // Given
        Page<DashboardDto.ParentQnaLogResponse> page = new PageImpl<>(
                List.of(DashboardDto.ParentQnaLogResponse.builder()
                        .id(1L)
                        .title("QnA Title")
                        .child("Child Name")
                        .createAt(LocalDateTime.now())
                        .build()
                ),
                PageRequest.of(0, 10),
                1
        );

        when(dashboardService.getQnaLog(anyLong(), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/dashboard")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("QnA Title"))
                .andExpect(jsonPath("$.content[0].child").value("Child Name"));
    }

    @Test
    @DisplayName("특정 자녀 QnA 보기 성공")
    @WithMockUser
    void getChildQna() throws Exception {
        // Given
        Long childId = 2L;
        Page<DashboardDto.ParentQnaLogResponse> page = new PageImpl<>(
                List.of(DashboardDto.ParentQnaLogResponse.builder()
                        .id(2L)
                        .title("Child's QnA Title")
                        .child("Child Name")
                        .createAt(LocalDateTime.now())
                        .build()
                ),
                PageRequest.of(0, 10),
                1
        );

        when(dashboardService.getChildQnaLog(eq(childId), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/dashboard/{childId}", childId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].title").value("Child's QnA Title"))
                .andExpect(jsonPath("$.content[0].child").value("Child Name"));
    }

    @Test
    @DisplayName("QnA 상세보기 성공")
    @WithMockUser
    void getQnaDetail() throws Exception {
        // Given
        Long qnaId = 3L;
        List<DashboardDto.QnAs> response = List.of(
                new DashboardDto.QnAs(1L, "Q1", "SA1", "A1"),
                new DashboardDto.QnAs(2L, "Q2", "SA2", "A2")
        );

        when(dashboardService.getQnaDetail(qnaId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/dashboard/detail/{qnaId}", qnaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].question").value("Q1"))
                .andExpect(jsonPath("$[0].sampleAnswer").value("SA1"))
                .andExpect(jsonPath("$[0].answer").value("A1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].question").value("Q2"))
                .andExpect(jsonPath("$[1].sampleAnswer").value("SA2"))
                .andExpect(jsonPath("$[1].answer").value("A2"));
    }
}