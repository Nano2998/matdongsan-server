package com.example.matdongsanserver.domain.dashboard.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.service.ChildService;
import com.example.matdongsanserver.domain.dashboard.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.dashboard.dto.DashboardDto;
import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.dashboard.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.dashboard.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import com.example.matdongsanserver.domain.story.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StoryQuestionRepository storyQuestionRepository;
    private final MemberRepository memberRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final ChildService childService;
    private final StoryRepository storyRepository;
    private final ExternalApiService externalApiService;

    /**
     * 동화 질문 생성
     * @param storyId
     * @return
     */
    @Transactional
    public DashboardDto.StoryQuestionResponse registerQuestions(String storyId) {
        Story story = storyRepository.findByIdOrThrow(storyId);

        // StoryQuestion 엔티티 생성 및 저장
        StoryQuestion storyQuestion = storyQuestionRepository.save(
                StoryQuestion.builder()
                        .storyId(storyId)
                        .language(story.getLanguage())
                        .build()
        );

        // 동화 질문 생성 요청 및 파싱
        List<Map<String, String>> parsedQuestions = externalApiService.sendQuestionRequest(
                story.getLanguage(),
                story.getAge(),
                story.getContent()
        );

        // QuestionAnswer 엔티티 생성 및 저장
        List<QuestionAnswer> questionAnswers = parsedQuestions.stream()
                .map(question -> QuestionAnswer.builder()
                        .question(question.get("Q").trim())
                        .sampleAnswer(question.get("A").trim())
                        .storyQuestion(storyQuestion)
                        .build())
                .toList();

        questionAnswerRepository.saveAll(questionAnswers);

        return DashboardDto.StoryQuestionResponse.builder()
                .storyquestion(storyQuestion)
                .build();
    }

    /**
     * 부모 qna로그 전체 가져오기
     * @param memberId
     * @return
     */
    public Page<DashboardDto.ParentQnaLogRequest> getQnaLog(Long memberId, Pageable pageable) {
        if(!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        List<ChildDto.ChildDetail> childDetails = childService.getChildDetails(memberId);

        List<Long> childIds = childDetails.stream()
                .map(ChildDto.ChildDetail::getId)
                .toList();

        Page<StoryQuestion> childQuestions = storyQuestionRepository.findAllByChildIdIn(childIds, pageable);

        List<String> storyIds = childQuestions.getContent().stream()
                .map(StoryQuestion::getStoryId)
                .distinct()
                .toList();

        List<Story> stories = storyRepository.findByIdIn(storyIds);

        Map<String, String> storyTitleMap = stories.stream()
                .collect(Collectors.toMap(Story::getId, Story::getTitle));

        return childQuestions.map(question -> DashboardDto.ParentQnaLogRequest.builder()
                .id(question.getId())
                .createAt(question.getCreatedAt())
                .title(storyTitleMap.get(question.getStoryId()))
                .child(question.getChild().getName())
                .build());
    }

    /**
     * 자녀 필터 QnA 로그 가져오기
     * @param childId
     * @param pageable
     * @return
     */
    public Page<DashboardDto.ParentQnaLogRequest> getChildQnaLog(Long childId, Pageable pageable) {
        Page<StoryQuestion> childQuestions = storyQuestionRepository.findByChildId(childId, pageable);

        List<String> storyIds = childQuestions.getContent().stream()
                .map(StoryQuestion::getStoryId)
                .distinct()
                .toList();

        List<Story> stories = storyRepository.findByIdIn(storyIds);

        Map<String, String> storyTitleMap = stories.stream()
                .collect(Collectors.toMap(Story::getId, Story::getTitle));

        return childQuestions.map(question -> DashboardDto.ParentQnaLogRequest.builder()
                .id(question.getId())
                .createAt(question.getCreatedAt())
                .title(storyTitleMap.get(question.getStoryId()))
                .child(question.getChild().getName())
                .build());
    }

    /**
     * QnA 상세 조회하기
     * @param qnaId
     * @return
     */
    public List<DashboardDto.QnAs> getQnaDetail(Long qnaId) {

        StoryQuestion storyQuestion = storyQuestionRepository.findByIdOrThrow(qnaId);

        return storyQuestion.getQuestionAnswers().stream()
                .map(DashboardDto.QnAs::new)
                .toList();
    }
}
