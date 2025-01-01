package com.example.matdongsanserver.domain.member.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.service.MemberService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(MemberController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private TokenProvider tokenProvider;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
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
    @DisplayName("회원가입 이후 닉네임 및 프로필 이미지 등록 성공")
    @WithMockUser
    void updateMember() throws Exception {
        // Given
        String nickname = "TestUser";
        MockMultipartFile profileImage = new MockMultipartFile(
                "multipartFile", "test-image.png", MediaType.IMAGE_PNG_VALUE, "image content".getBytes()
        );

        MemberDto.MemberDetail mockResponse = createMockResponse();

        when(memberService.updateMember(anyLong(), any(String.class), any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/api/members")
                        .file(profileImage)
                        .param("nickname", nickname)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.profileImage").value("http://test.com/test-image.png"));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser
    void getMemberDetail() throws Exception {
        // Given
        MemberDto.MemberDetail mockResponse = createMockResponse();

        when(memberService.getMemberDetail(anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/members")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.profileImage").value("http://test.com/test-image.png"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("다른 작가의 정보 조회 성공")
    @WithMockUser
    void getOtherMemberDetail() throws Exception {
        // Given
        MemberDto.MemberDetailOther mockResponse = MemberDto.MemberDetailOther.builder()
                .member(Member.builder()
                        .nickname("OtherUser")
                        .profileImage("http://test.com/other-image.png")
                        .email("test@test.com")
                        .role(Role.USER)
                        .build())
                .storyCount(3L)
                .likeCount(15L)
                .isFollowed(true)
                .build();

        when(memberService.getOtherMemberDetail(anyLong(), anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/members/{memberId}", 2L)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("OtherUser"))
                .andExpect(jsonPath("$.profileImage").value("http://test.com/other-image.png"))
                .andExpect(jsonPath("$.isFollowed").value(true));
    }

    private MemberDto.MemberDetail createMockResponse() {
        return MemberDto.MemberDetail.builder()
                .member(Member.builder()
                        .nickname("TestUser")
                        .profileImage("http://test.com/test-image.png")
                        .email("test@test.com")
                        .role(Role.USER)
                        .build())
                .storyCount(3L)
                .likeCount(15L)
                .build();
    }
}