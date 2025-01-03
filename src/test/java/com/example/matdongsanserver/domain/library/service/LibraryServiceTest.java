package com.example.matdongsanserver.domain.library.service;

import com.example.matdongsanserver.domain.library.AgeType;
import com.example.matdongsanserver.domain.library.LangType;
import com.example.matdongsanserver.domain.library.SortType;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("라이브러리 서비스 테스트")
class LibraryServiceTest {

    @InjectMocks
    LibraryService libraryService;
    @Mock
    StoryLikeRepository storyLikeRepository;
    @Mock
    StoryRepository storyRepository;
    @Mock
    RedisTemplate<String, String> redisTemplate;

    private Member testMember;
    private static final int MAX_RECENT_TALES = 50;
    private static final int TTL_DAYS = 5;


    @BeforeEach
    void setUp() {
        testMember = createMember("test@naver.com","test","testImg");
    }

    @Test
    @DisplayName("동화 조회 성공 - 최신순")
    void getStories() {
        // Given
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화", "3번동화"));
        when(storyRepository.findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByCreatedAtDesc(anyInt(), anyInt(), anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(stories));

        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.DESC, "createAt");

        // When
        Page<StoryDto.StorySummary> result = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("1번동화");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("2번동화");
    }

    @Test
    @DisplayName("동화 조회 성공 - 인기순")
    void getStories1() {
        // Mock 데이터 준비
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화", "3번동화"));

        // 좋아요 추가 (addLikes 메서드 사용)
        Story story1 = stories.get(0);
        story1.addLikes(); // 좋아요 수 증가

        // Mocking: 인기순으로 조회
        when(storyRepository.findByIsPublicTrueAndAgeBetweenAndLanguageInOrderByLikesDesc(
                anyInt(), anyInt(), anyList(), any(Pageable.class))
        ).thenReturn(new PageImpl<>(List.of(story1, stories.get(1), stories.get(2))));

        // When
        Page<StoryDto.StorySummary> popularStories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.POPULAR, Pageable.unpaged());

        // Then
        assertThat(popularStories.getContent()).hasSize(3); // 3개의 동화가 반환됨
        assertThat(popularStories.getContent().get(0).getLikes()).isEqualTo(1); // 첫 번째 동화는 좋아요가 1
        assertThat(popularStories.getContent().get(1).getLikes()).isEqualTo(0); // 두 번째 동화는 좋아요가 0
        assertThat(popularStories.getContent().get(2).getLikes()).isEqualTo(0); // 세 번째 동화도 좋아요가 0
    }

    @Test
    @DisplayName("특정 작가 동화 조회 성공")
    void getStoriesByAuthorId() {
        // Given
        Member member = createMember("test2@naver.com", "test2", "test2Img");
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화"));

        when(storyRepository.findByIsPublicTrueAndMemberIdOrderByCreatedAtDesc(
                eq(testMember.getId()), any(Pageable.class)) // 모든 인자를 매처로 사용
        ).thenReturn(new PageImpl<>(stories));

        // When
        Page<StoryDto.StorySummary> storiesByAuthorId = libraryService.getStoriesByAuthorId(testMember.getId(), SortType.RECENT, Pageable.unpaged());

        // Then
        assertThat(storiesByAuthorId.getContent().get(0).getAuthor()).isEqualTo(testMember.getNickname());
        assertThat(storiesByAuthorId.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("내 동화 조회 성공")
    void getMyStories() {
        // Given
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화", "3번동화"));
        when(storyRepository.findByMemberIdOrderByCreatedAtDesc(
                eq(testMember.getId()), any(Pageable.class)) // 모든 인자를 매처로 사용
        ).thenReturn(new PageImpl<>(stories));

        // When
        Page<StoryDto.StorySummary> storiesByAuthorId = libraryService.getMyStories(testMember.getId(), SortType.RECENT, Pageable.unpaged());

        // Then
        assertThat(storiesByAuthorId.getContent().get(0).getAuthor()).isEqualTo(testMember.getNickname());
        assertThat(storiesByAuthorId.getContent()).hasSize(3);

    }

    @Test
    @DisplayName("좋아요 누른 동화 리스트")
    void getLikedStories() {
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화", "3번동화"));

        // Mock 좋아요 데이터를 반환하도록 설정
        List<StoryLike> mockStoryLikes = List.of(
                new StoryLike(stories.get(0).getId(),testMember),
                new StoryLike(stories.get(1).getId(),testMember)
        );
        when(storyLikeRepository.findByMemberId(eq(testMember.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockStoryLikes));

        when(storyRepository.findByIdIn(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(stories.subList(0, 2)));

        Pageable pageable = Pageable.unpaged();

        // When
        Page<StoryDto.StorySummary> likedStories = libraryService.getLikedStories(testMember.getId(), pageable);

        // Then
        assertThat(likedStories.getContent()).hasSize(2);
        assertThat(likedStories.getContent())
                .extracting("title")
                .containsExactlyInAnyOrder("1번동화", "2번동화");
    }

    @Test
    @DisplayName("최근 본 동화 추가")
    void addRecentStories() {
        // Given
        List<Story> stories = createStories(testMember, List.of("1번동화", "2번동화", "3번동화"));
        String storyId = stories.get(1).getId();
        String redisKey = "user:" + testMember.getId() + ":recentTales";

        // ListOperations Mock 생성
        ListOperations<String, String> listOperationsMock = mock(ListOperations.class);

        // redisTemplate의 opsForList() 호출 결과를 Mock으로 설정
        when(redisTemplate.opsForList()).thenReturn(listOperationsMock);

        // listOperationsMock의 동작 설정
        when(listOperationsMock.remove(redisKey, 0, storyId)).thenReturn(1L); // 중복 제거
        when(listOperationsMock.leftPush(redisKey, storyId)).thenReturn(1L); // 리스트 추가
        doNothing().when(listOperationsMock).trim(redisKey, 0, MAX_RECENT_TALES - 1);
        when(redisTemplate.expire(redisKey, TTL_DAYS, TimeUnit.DAYS)).thenReturn(true); // TTL 설정

        // When
        libraryService.addRecentStories(testMember.getId(), storyId);

        // Then
        verify(listOperationsMock).remove(redisKey, 0, storyId); // 호출 검증
        verify(listOperationsMock).leftPush(redisKey, storyId); // 호출 검증
        verify(listOperationsMock).trim(redisKey, 0, MAX_RECENT_TALES - 1); // 호출 검증
        verify(redisTemplate).expire(redisKey, TTL_DAYS, TimeUnit.DAYS); // 호출 검증
    }

    @Test
    @DisplayName("최근 본 동화리스트 조회")
    void getRecentStories() {
        // TODO: 2025/01/2  - Nano
    }


    @Test
    @DisplayName("태그 동화 검색 성공")
    void searchStories() {
        // TODO: 2025/01/2  - Nano
    }


    // 멤버 추가
    private Member createMember(String email, String nickname, String profileImage) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .role(Role.USER)
                .build();
    }

    // 동화 추가
    private List<Story> createStories(Member member, List<String> titles) {
        return titles.stream()
                .map(title -> Story.builder()
                        .age(3)
                        .author(member.getNickname())
                        .title(title)
                        .content("테스트 내용")
                        .memberId(member.getId())
                        .given("테스트 프롬프트")
                        .language(Language.EN)
                        .build())
                .toList();
    }
}