package com.example.matdongsanserver.domain.library.service;

import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.library.AgeType;
import com.example.matdongsanserver.domain.library.LangType;
import com.example.matdongsanserver.domain.library.SortType;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import com.example.matdongsanserver.domain.story.service.StoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("라이브러리 서비스 테스트")
class LibraryServiceTest {

    @Autowired
    StoryLikeRepository storyLikeRepository;
    @Autowired
    StoryRepository storyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    StoryService storyService;
    @Autowired
    LibraryService libraryService;

    @BeforeEach
    void setUp() {
        storyLikeRepository.deleteAll();
        storyRepository.deleteAll();
        redisTemplate.delete("*");
    }

    @Test
    @DisplayName("동화 조회 성공 - 최신순")
    void getStories() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(),member.getId(), "1번동화", Language.EN, 3);
        createStory(member.getNickname(),member.getId(), "2번동화", Language.EN, 3);
        createStory(member.getNickname(),member.getId(), "3번동화", Language.EN, 3);

        // When
        Pageable pageable = PageRequest.of(0, 2, Sort.Direction.DESC, "createAt");
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, pageable);

        // Then
        assertThat(stories.getContent()).hasSize(2);
        assertThat(stories.getContent().get(0).getTitle()).isEqualTo("3번동화");
        assertThat(stories.getContent().get(1).getTitle()).isEqualTo("2번동화");
        assertThat(stories.hasNext()).isTrue();
    }

    @Test
    @DisplayName("동화 조회 성공 - 인기순")
    void getStories1() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(), member.getId(), "1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(), "2번동화", Language.EN, 4);

        // 좋아요 추가
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();
        storyService.addLike(storyId,member.getId());

        // When
        Page<StoryDto.StorySummary> popularStories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.POPULAR, Pageable.unpaged());

        // Then
        assertThat(popularStories.getContent().get(0).getLikes()).isEqualTo(1);
        assertThat(popularStories.getContent().get(1).getLikes()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 작가 동화 조회 성공")
    void getStoriesByAuthorId() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        createStory(member.getNickname(), member.getId(),"1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"2번동화", Language.EN, 3);
        createStory(member2.getNickname(), member2.getId(),"1번동화", Language.EN, 3);

        // When
        Page<StoryDto.StorySummary> storiesByAuthorId = libraryService.getStoriesByAuthorId(member2.getId(), SortType.RECENT, Pageable.unpaged());

        // Then
        assertThat(storiesByAuthorId.getContent().get(0).getAuthor()).isEqualTo(member2.getNickname());
        assertThat(storiesByAuthorId.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("내 동화 조회 성공")
    void getMyStories() {
        // Given
        Member member = createAndSaveMember();
        Member member2 = createAndSaveMember2();
        createStory(member.getNickname(), member.getId(),"1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"2번동화", Language.EN, 3);
        createStory(member2.getNickname(), member2.getId(),"1번동화", Language.EN, 3);

        // When
        Page<StoryDto.StorySummary> storiesByAuthorId = libraryService.getMyStories(member2.getId(), SortType.RECENT, Pageable.unpaged());

        // Then
        assertThat(storiesByAuthorId.getContent().get(0).getAuthor()).isEqualTo(member2.getNickname());
        assertThat(storiesByAuthorId.getContent()).hasSize(1);

    }

    @Test
    @DisplayName("좋아요 누른 동화 리스트")
    void getLikedStories() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(), member.getId(), "1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(), "2번동화", Language.EN, 4);
        createStory(member.getNickname(), member.getId(), "3번동화", Language.EN, 4);
        createStory(member.getNickname(), member.getId(), "4번동화", Language.EN, 4);

        // 좋아요 추가
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();
        String storyId2 = stories.getContent().get(1).getId();
        storyService.addLike(storyId,member.getId());
        storyService.addLike(storyId2,member.getId());

        // When
        Page<StoryDto.StorySummary> likedStories = libraryService.getLikedStories(member.getId(), Pageable.unpaged());

        // Then
        assertThat(likedStories.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("최근 본 동화 추가")
    void addRecentStories() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(), member.getId(),"1번동화", Language.EN, 3);

        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();

        // When
        libraryService.addRecentStories(member.getId(), storyId);
        List<StoryDto.StorySummary> recentStories = libraryService.getRecentStories(member.getId());

        // Then
        assertThat(recentStories.get(0).getTitle()).isEqualTo("1번동화");
    }

    @Test
    @DisplayName("최근 본 동화리스트 조회")
    void getRecentStories() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(), member.getId(),"1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"2번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"3번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"4번동화", Language.EN, 3);

        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();
        String storyId2 = stories.getContent().get(1).getId();

        // When
        libraryService.addRecentStories(member.getId(), storyId);
        libraryService.addRecentStories(member.getId(), storyId2);
        List<StoryDto.StorySummary> recentStories = libraryService.getRecentStories(member.getId());

        // Then
        assertThat(recentStories.size()).isEqualTo(2);
        assertThat(recentStories.get(0).getTitle()).isEqualTo("3번동화");
    }

    @Test
    @DisplayName("태그 동화 검색 성공")
    void searchStories() {
        // Given
        Member member = createAndSaveMember();
        createStory(member.getNickname(), member.getId(),"1번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"2번동화", Language.EN, 3);
        createStory(member.getNickname(), member.getId(),"3번동화", Language.EN, 3);
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.EN, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();

        // 동화 태그 업데이트
        List<String> tags = List.of("사람", "테스트");
        StoryDto.StoryUpdateRequest storyUpdateRequest = StoryDto.StoryUpdateRequest.builder()
                .tags(tags)
                .isPublic(true)
                .title("3번동화")
                .build();
        storyService.updateStoryDetail(member.getId(), storyId, storyUpdateRequest);

        // When
        Page<Story> searchResults = storyRepository.findByTags(tags, Pageable.unpaged());

        // Then
        assertThat(searchResults.getSize()).isEqualTo(1);
        assertThat(searchResults.getContent().get(0).getTitle()).isEqualTo("3번동화");
        assertThat(searchResults.getContent().get(0).getTags()).hasSize(2);
        assertThat(searchResults.getContent().get(0).getTags()).contains("사람","테스트");
    }


    // 멤버 추가 1
    private Member createAndSaveMember() {
        Member member = Member.builder()
                .email("test@naver.com")
                .nickname("test")
                .profileImage("testImg")
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    // 멤버 추가 2
    private Member createAndSaveMember2() {
        Member member = Member.builder()
                .email("test2@naver.com")
                .nickname("test2")
                .profileImage("test2Img")
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    // 동화 생성
    private void createStory(String author, Long memberId, String title, Language language, int age) {
        storyRepository.save(Story.builder()
                .age(age)
                .author(author)
                .title(title)
                .content("테스트내용")
                .memberId(memberId)
                .given("테스트프롬프트")
                .language(language)
                .build());
    }
}