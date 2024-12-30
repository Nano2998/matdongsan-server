package com.example.matdongsanserver.domain.follow.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.follow.service.FollowService;
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
import com.example.matdongsanserver.domain.member.dto.MemberDto;

import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(FollowController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @MockBean
    private TokenProvider tokenProvider;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

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
    @DisplayName("팔로우 성공")
    @WithMockUser
    void follow() throws Exception {
        // Given
        Long followerId = 2L;

        doNothing().when(followService).follow(1L, followerId);

        // When & Then
        mockMvc.perform(post("/api/members/follow/{followerId}", followerId))
                .andExpect(status().isNoContent());
        verify(followService, times(1)).follow(1L, followerId);
    }

    @Test
    @DisplayName("언팔로우 성공")
    @WithMockUser
    void unfollow() throws Exception {
        // Given
        Long followerId = 2L;

        doNothing().when(followService).unfollow(1L, followerId);

        // When & Then
        mockMvc.perform(delete("/api/members/follow/{followerId}", followerId))
                .andExpect(status().isNoContent());
        verify(followService, times(1)).unfollow(1L, followerId);
    }

    @Test
    @DisplayName("팔로우 리스트 조회 성공")
    @WithMockUser
    void getFollowers() throws Exception {
        // Given
        List<MemberDto.MemberSummary> mockFollowers = List.of(
                new MemberDto.MemberSummary(1L, "http://test.com/image1.png", "User1", 5L),
                new MemberDto.MemberSummary(2L, "http://test.com/image2.png", "User2", 3L)
        );

        when(followService.getFollowers(1L)).thenReturn(mockFollowers);

        // When & Then
        mockMvc.perform(get("/api/members/follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].profileImage").value("http://test.com/image1.png"))
                .andExpect(jsonPath("$[0].nickname").value("User1"))
                .andExpect(jsonPath("$[0].followers").value(5L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].profileImage").value("http://test.com/image2.png"))
                .andExpect(jsonPath("$[1].nickname").value("User2"))
                .andExpect(jsonPath("$[1].followers").value(3L));
    }
}