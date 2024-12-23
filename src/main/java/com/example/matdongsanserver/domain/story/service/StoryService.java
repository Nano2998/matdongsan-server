package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.common.config.PromptsConfig;
import com.example.matdongsanserver.domain.library.service.LibraryService;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.common.external.ExternalApiRequest;
import com.example.matdongsanserver.domain.story.entity.StoryLike;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private final PromptsConfig promptsConfig;
    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final LibraryService libraryService;
    private final ExternalApiRequest externalApiRequest;
    private final StoryCacheService storyCacheService;

    /**
     * 동화 생성
     * @param memberId
     * @param requestDto
     * @return
     */
    @Transactional
    public StoryDto.StoryCreationResponse registerStory(Long memberId, StoryDto.StoryCreationRequest requestDto) {
        Language language = Language.fromString(requestDto.getLanguage());
        Member member = memberRepository.findByIdOrThrow(memberId);

        // 프롬프트와 토큰 설정
        String prompt = getPromptForAge(requestDto.getAge(), language, requestDto.getGiven());

        // 동화 생성 요청 및 응답 파싱
        Map<String, String> storyDetails = externalApiRequest.sendStoryCreationRequest(prompt, language);

        Story save = storyRepository.save(Story.builder()
                .age(requestDto.getAge())
                .language(language)
                .given(requestDto.getGiven())
                .title(storyDetails.get("title"))
                .content(storyDetails.get("content"))
                .memberId(memberId)
                .author(member.getNickname())
                .coverUrl("")
                .build());

        // 동화 요약 및 커버 이미지 생성 요청
        String summary = externalApiRequest.sendSummaryRequest(storyDetails.get("content"));
        save.updateCoverUrl(externalApiRequest.sendImageRequest(save.getId(), summary));
        storyRepository.save(save);

        // 생성된 동화를 최근 동화에 포함
        libraryService.addRecentStories(memberId,save.getId());

        return StoryDto.StoryCreationResponse.builder()
                .story(save)
                .build();
    }

    /**
     * 입력 받은 테마와 나이, 언어를 통해 프롬프트 제공
     * @param age
     * @param language
     * @param given
     * @return
     */
    private String getPromptForAge(int age, Language language, String given) {
        Map<Integer, String> templates = switch (language) {
            case EN -> promptsConfig.getEn();
            case KO -> promptsConfig.getKo();
        };

        String template = templates.get(age);
        if (template == null) {
            throw new StoryException(StoryErrorCode.INVALID_AGE);
        }

        return template.replace("{given}", given);
    }

    /**
     * 동화 상세 조회
     * @param storyId
     * @param memberId
     * @return
     */
    public StoryDto.StoryDetail getStoryDetail(String storyId, Long memberId) {
        Story story = storyCacheService.getStory(storyId);

        // 조회하는 동화를 최근 동화에 포함
        libraryService.addRecentStories(memberId, storyId);

        return StoryDto.StoryDetail.builder()
                .story(story)
                .isLiked(storyLikeRepository.existsByStoryIdAndMemberId(storyId, memberId))
                .build();
    }

    /**
     * 동화 상세 수정
     * @param memberId
     * @param storyId
     * @param requestDto
     * @return
     */
    @Transactional
    public StoryDto.StoryDetail updateStoryDetail(Long memberId, String storyId, StoryDto.StoryUpdateRequest requestDto) {
        Story story = storyCacheService.updateStory(storyId, requestDto);

        if (!story.getMemberId().equals(memberId)) {
            throw new StoryException(StoryErrorCode.STORY_EDIT_PERMISSION_DENIED);  // 동화의 주인만 동화 상세 수정 가능
        }

        return StoryDto.StoryDetail.builder()
                .story(storyRepository.save(story))
                .isLiked(storyLikeRepository.existsByStoryIdAndMemberId(storyId, memberId))
                .build();
    }

    /**
     * 동화 좋아요
     * @param storyId
     * @param memberId
     */
    @Transactional
    public void likeStory(String storyId, Long memberId) {
        Story story = storyCacheService.likeStory(storyId);

        if (storyLikeRepository.findByStoryIdAndMemberId(storyId, memberId).isPresent()) {
            throw new StoryException(StoryErrorCode.LIKE_ALREADY_EXISTS);   // 이미 좋아요를 누른 경우
        }

        storyLikeRepository.save(StoryLike.builder()
                .storyId(storyId)
                .member(memberRepository.findByIdOrThrow(memberId))
                .build());

        storyRepository.save(story);
    }

    /**
     * 동화 좋아요 취소
     * @param storyId
     * @param memberId
     */
    @Transactional
    public void unlikeStory(String storyId, Long memberId) {
        Story story = storyCacheService.unlikeStory(storyId);

        storyLikeRepository.delete(storyLikeRepository.findByStoryIdAndMemberId(storyId, memberId)
                .orElseThrow(
                        () -> new StoryException(StoryErrorCode.LIKE_NOT_EXISTS)    //좋아요를 누르지 않고 취소 시도
                ));

        storyRepository.save(story);
    }

    /**
     * 동화 TTS 반환 - TTS가 이미 있다면 그대로 전달, 없다면 TTS 생성 요청 후 전달
     * @param storyId
     * @return
     */
    @Transactional
    public StoryDto.TTSResponse getOrRegisterStoryTTS(String storyId) {
        Story story = storyRepository.findByIdOrThrow(storyId);

        // 이미 해당 동화의 TTS가 저장되어 있다면 반환
        if (!story.getTtsUrl().isBlank()){
            return StoryDto.TTSResponse.builder()
                    .ttsUrl(story.getTtsUrl())
                    .timestamps(story.getTimestamps())
                    .build();
        }

        StoryDto.TTSResponse ttsResponse = externalApiRequest.sendTTSRequest(
                storyId, story.getContent(), story.getLanguage(), "tts"
        );
        storyRepository.save(story.updateTTSUrl(ttsResponse.getTtsUrl(), ttsResponse.getTimestamps()));
        return ttsResponse;
    }
}
