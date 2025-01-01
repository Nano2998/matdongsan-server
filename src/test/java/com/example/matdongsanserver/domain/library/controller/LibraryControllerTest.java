package com.example.matdongsanserver.domain.library.controller;

import com.example.matdongsanserver.TestSecurityConfig;
import com.example.matdongsanserver.common.utils.SecurityUtils;
import com.example.matdongsanserver.domain.auth.jwt.TokenProvider;
import com.example.matdongsanserver.domain.library.AgeType;
import com.example.matdongsanserver.domain.library.LangType;
import com.example.matdongsanserver.domain.library.SortType;
import com.example.matdongsanserver.domain.library.service.LibraryService;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
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

import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(LibraryController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryService libraryService;

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
    @DisplayName("조건별 동화 리스트 조회 성공")
    @WithMockUser
    void getStories() throws Exception {
        // Given
        Page<StoryDto.StorySummary> mockPage = new PageImpl<>(
                List.of(new StoryDto.StorySummary("1", "Story1", 10L, "http://cover.url/1", List.of("tag1", "tag2"), "Author1")),
                PageRequest.of(0, 10),
                1
        );

        when(libraryService.getStories(any(AgeType.class), any(LangType.class), any(SortType.class), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/library")
                        .param("sortBy", "recent")
                        .param("language", "all")
                        .param("age", "main")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].title").value("Story1"))
                .andExpect(jsonPath("$.content[0].likes").value(10))
                .andExpect(jsonPath("$.content[0].coverUrl").value("http://cover.url/1"))
                .andExpect(jsonPath("$.content[0].tags[0]").value("tag1"))
                .andExpect(jsonPath("$.content[0].author").value("Author1"));
    }

    @Test
    @DisplayName("특정 작가의 동화 리스트 조회 성공")
    @WithMockUser
    void getStoriesByWriter() throws Exception {
        // Given
        Long authorId = 2L;
        Page<StoryDto.StorySummary> mockPage = new PageImpl<>(
                List.of(new StoryDto.StorySummary("2", "Story2", 15L, "http://cover.url/2", List.of("tagA", "tagB"), "Author2")),
                PageRequest.of(0, 10),
                1
        );

        when(libraryService.getStoriesByAuthorId(eq(authorId), any(SortType.class), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/library/{authorId}", authorId)
                        .param("sortBy", "popular")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("2"))
                .andExpect(jsonPath("$.content[0].title").value("Story2"))
                .andExpect(jsonPath("$.content[0].likes").value(15))
                .andExpect(jsonPath("$.content[0].tags[0]").value("tagA"))
                .andExpect(jsonPath("$.content[0].author").value("Author2"));
    }

    @Test
    @DisplayName("내 동화 리스트 조회 성공")
    @WithMockUser
    void getMyStories() throws Exception {
        // Given
        Page<StoryDto.StorySummary> mockPage = new PageImpl<>(
                List.of(new StoryDto.StorySummary("3", "Story3", 20L, "http://cover.url/3", List.of("tagX", "tagY"), "Author3")),
                PageRequest.of(0, 10),
                1
        );

        when(libraryService.getMyStories(eq(1L), any(SortType.class), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/library/my")
                        .param("sortBy", "recent")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("3"))
                .andExpect(jsonPath("$.content[0].title").value("Story3"))
                .andExpect(jsonPath("$.content[0].likes").value(20))
                .andExpect(jsonPath("$.content[0].tags[0]").value("tagX"))
                .andExpect(jsonPath("$.content[0].author").value("Author3"));
    }

    @Test
    @DisplayName("좋아요 누른 동화 리스트 조회 성공")
    @WithMockUser
    void getLikedStories() throws Exception {
        // Given
        Page<StoryDto.StorySummary> mockPage = new PageImpl<>(
                List.of(new StoryDto.StorySummary("4", "Story4", 25L, "http://cover.url/4", List.of("tagL", "tagM"), "Author4")),
                PageRequest.of(0, 10),
                1
        );

        when(libraryService.getLikedStories(eq(1L), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/library/likes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("4"))
                .andExpect(jsonPath("$.content[0].title").value("Story4"))
                .andExpect(jsonPath("$.content[0].likes").value(25))
                .andExpect(jsonPath("$.content[0].tags[0]").value("tagL"))
                .andExpect(jsonPath("$.content[0].author").value("Author4"));
    }
}