package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.common.external.ExternalApiRequest;
import com.example.matdongsanserver.domain.follow.repository.FollowRepository;
import com.example.matdongsanserver.domain.library.service.LibraryService;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("동화 서비스 테스트")
class StoryServiceTest {

    @Mock
    private PromptsConfig promptsConfig;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StoryLikeRepository storyLikeRepository;

    @Mock
    private LibraryService libraryService;

    @Mock
    private ExternalApiRequest externalApiRequest;

    @Mock
    private StoryCacheService storyCacheService;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private StoryService storyService;

    @Test
    @DisplayName("동화 생성 성공")
    void registerStory_success() {
        // Given
        Member mockMember = mock(Member.class);
        when(mockMember.getNickname()).thenReturn("TestUser");

        Story mockStory = mock(Story.class);
        when(mockStory.getId()).thenReturn("story123");
        when(mockStory.getTitle()).thenReturn("Generated Title");
        when(mockStory.getContent()).thenReturn("Generated Content");
        when(mockStory.getAuthor()).thenReturn("TestUser");

        StoryDto.StoryCreationRequest request = new StoryDto.StoryCreationRequest("EN", 5, "Fantasy");
        Map<String, String> storyDetails = Map.of(
                "title", "Generated Title",
                "content", "Generated Content"
        );

        when(memberRepository.findByIdOrThrow(1L)).thenReturn(mockMember);
        when(promptsConfig.getEn()).thenReturn(Map.of(5, "Prompt for 5 years old"));
        when(externalApiRequest.sendStoryCreationRequest(anyString(), any())).thenReturn(storyDetails);
        when(storyRepository.save(any(Story.class))).thenReturn(mockStory);

        // When
        StoryDto.StoryCreationResponse response = storyService.registerStory(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Generated Title");
        assertThat(response.getContent()).isEqualTo("Generated Content");
        assertThat(response.getAuthor()).isEqualTo("TestUser");
        verify(libraryService).addRecentStories(1L, "story123");
    }

    @Test
    @DisplayName("동화 상세 조회 성공")
    void getStoryDetail_success() {
        // Given
        Story mockStory = mock(Story.class);
        when(mockStory.getId()).thenReturn("story123");
        when(mockStory.getTitle()).thenReturn("Test Story");
        when(mockStory.getContent()).thenReturn("Once upon a time...");
        when(mockStory.getLanguage()).thenReturn(Language.EN);
        when(mockStory.getMemberId()).thenReturn(1L);
        when(mockStory.getAuthor()).thenReturn("TestUser");

        when(storyCacheService.getStory("story123")).thenReturn(mockStory);
        when(storyLikeRepository.existsByStoryIdAndMemberId("story123", 1L)).thenReturn(true);
        when(followRepository.existsByFollowingIdAndFollowerId(1L, 1L)).thenReturn(false);

        // When
        StoryDto.StoryDetail result = storyService.getStoryDetail("story123", 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Story");
        assertThat(result.getIsLiked()).isTrue();
        assertThat(result.getIsFollowed()).isFalse();
        assertThat(result.getIsMyStory()).isTrue();
    }

    @Test
    @DisplayName("동화 좋아요 성공")
    void likeStory_success() {
        // Given
        Member mockMember = mock(Member.class);
        Story mockStory = mock(Story.class);

        when(storyCacheService.likeStory("story123")).thenReturn(mockStory);
        when(storyLikeRepository.findByStoryIdAndMemberId("story123", 1L)).thenReturn(Optional.empty());
        when(memberRepository.findByIdOrThrow(1L)).thenReturn(mockMember);

        // When
        storyService.likeStory("story123", 1L);

        // Then
        verify(storyLikeRepository).save(any(StoryLike.class));
        verify(storyRepository).save(mockStory);
    }

    @Test
    @DisplayName("동화 좋아요 실패 - 이미 좋아요한 경우")
    void likeStory_alreadyLiked() {
        // Given
        StoryLike mockStoryLike = mock(StoryLike.class);
        when(storyLikeRepository.findByStoryIdAndMemberId("story123", 1L)).thenReturn(Optional.ofNullable(mockStoryLike));

        // When
        StoryException storyException = assertThrows(StoryException.class, () ->
                storyService.likeStory("story123", 1L)
        );

        // Then
        assertThat(storyException.getErrorCode())
                .isEqualTo(StoryErrorCode.LIKE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("동화 좋아요 취소 성공")
    void unlikeStory_success() {
        // Given
        Member mockMember = mock(Member.class);
        Story mockStory = mock(Story.class);

        when(storyCacheService.unlikeStory("story123")).thenReturn(mockStory);
        when(storyLikeRepository.findByStoryIdAndMemberId("story123", 1L))
                .thenReturn(Optional.of(StoryLike.builder().storyId("story123").member(mockMember).build()));

        // When
        storyService.unlikeStory("story123", 1L);

        // Then
        verify(storyLikeRepository).delete(any(StoryLike.class));
        verify(storyRepository).save(mockStory);
    }

    @Test
    @DisplayName("동화 좋아요 취소 실패 - 좋아요하지 않은 경우")
    void unlikeStory_notLiked() {
        // Given
        when(storyLikeRepository.findByStoryIdAndMemberId("story123", 1L)).thenReturn(Optional.empty());

        // When
        StoryException storyException = assertThrows(StoryException.class, () ->
                storyService.unlikeStory("story123", 1L)
        );

        // Then
        assertThat(storyException.getErrorCode())
                .isEqualTo(StoryErrorCode.LIKE_NOT_EXISTS);
    }
}
