package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
@ActiveProfiles("test")
class StoryRepositoryTest {

    @Autowired
    private StoryRepository storyRepository;

    @Test
    @DisplayName("동화 조회 성공")
    void findByIdOrThrow_Success() {
        // Given
        Story story = createStory("testTitle", 1L, null, false);

        // When
        Story findStory = storyRepository.findByIdOrThrow(story.getId());

        // Then
        assertThat(findStory.getTitle()).isEqualTo("testTitle");
        assertThat(findStory.getContent()).isEqualTo("testContent");
    }

    @Test
    @DisplayName("동화 조회 실패 - 존재하지 않는 동화 조회")
    void findByIdOrThrow_Notfound() {
        // Given
        String nonExistStoryId = "-1";

        // When
        StoryException storyException = assertThrows(StoryException.class, () ->
                storyRepository.findByIdOrThrow(nonExistStoryId)
        );

        // Then
        assertThat(storyException.getErrorCode())
                .isEqualTo(StoryErrorCode.STORY_NOT_FOUND);
    }

    @Test
    @DisplayName("태그 검색")
    void findByTags() {
        // Given
        createStory("fixedTitle1", 0L, Arrays.asList("adventure", "fun"), true);
        createStory("fixedTitle2", 0L, Arrays.asList("adventure", "learning"), true);

        // When
        Page<Story> test1 = storyRepository.findByTags(List.of("adventure"), PageRequest.of(0, 10));
        Page<Story> test2 = storyRepository.findByTags(Arrays.asList("adventure", "fun"), PageRequest.of(0, 10));
        Page<Story> test3 = storyRepository.findByTags(Arrays.asList("ad", "f"), PageRequest.of(0, 10));

        // Then
        assertThat(test1.getTotalElements()).isEqualTo(2);
        assertThat(test2.getTotalElements()).isEqualTo(2);
        assertThat(test3.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 작가의 좋아요 수 조회")
    void sumLikesByMemberId() {
        // Given
        Story story1 = createStory("testTitle1", 0L, null, false);
        Story story2 = createStory("testTitle2", 0L, null, false);

        story1.addLikes();
        story2.addLikes();
        storyRepository.saveAll(List.of(story1, story2));

        // When
        Long totalLikes = storyRepository.sumLikesByMemberId(0L);

        // Then
        assertThat(totalLikes).isEqualTo(2);
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
        return storyRepository.save(story);
    }
}