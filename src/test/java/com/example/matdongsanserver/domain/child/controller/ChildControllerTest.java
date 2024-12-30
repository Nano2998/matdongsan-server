package com.example.matdongsanserver.domain.child.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.service.ChildService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ChildController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ChildControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChildService childService;

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
    @DisplayName("자녀 추가 성공")
    @WithMockUser
    void registerChild() throws Exception {
        // Given
        ChildDto.ChildRequest childRequest = new ChildDto.ChildRequest("Child1", 3, 5);
        List<ChildDto.ChildDetail> response = List.of(new ChildDto.ChildDetail(1L, "Child1", 3, 5));

        when(childService.registerChild(anyLong(), any(ChildDto.ChildRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/members/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(childRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Child1"))
                .andExpect(jsonPath("$[0].englishAge").value(3))
                .andExpect(jsonPath("$[0].koreanAge").value(5));
    }

    @Test
    @DisplayName("자녀 조회 성공")
    @WithMockUser
    void getChildDetails() throws Exception {
        // Given
        List<ChildDto.ChildDetail> response = List.of(
                new ChildDto.ChildDetail(1L, "Child1", 3, 5),
                new ChildDto.ChildDetail(2L, "Child2", 6, 8)
        );

        when(childService.getChildDetails(anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/members/children")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Child1"))
                .andExpect(jsonPath("$[0].englishAge").value(3))
                .andExpect(jsonPath("$[0].koreanAge").value(5))
                .andExpect(jsonPath("$[1].name").value("Child2"))
                .andExpect(jsonPath("$[1].englishAge").value(6))
                .andExpect(jsonPath("$[1].koreanAge").value(8));
    }

    @Test
    @DisplayName("자녀 정보 수정 성공")
    @WithMockUser
    void updateChild() throws Exception {
        // Given
        Long childId = 1L;
        ChildDto.ChildRequest childRequest = new ChildDto.ChildRequest("UpdatedChild", 4, 7);
        List<ChildDto.ChildDetail> response = List.of(new ChildDto.ChildDetail(1L, "UpdatedChild", 4, 7));

        when(childService.updateChild(anyLong(), eq(childId), any(ChildDto.ChildRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/members/children/{childId}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(childRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("UpdatedChild"))
                .andExpect(jsonPath("$[0].englishAge").value(4))
                .andExpect(jsonPath("$[0].koreanAge").value(7));
    }

    @Test
    @DisplayName("자녀 삭제 성공")
    @WithMockUser
    void deleteChild() throws Exception {
        // Given
        Long childId = 1L;

        doNothing().when(childService).deleteChild(anyLong(), eq(childId));

        // When & Then
        mockMvc.perform(delete("/api/members/children/{childId}", childId))
                .andExpect(status().isNoContent());
    }
}