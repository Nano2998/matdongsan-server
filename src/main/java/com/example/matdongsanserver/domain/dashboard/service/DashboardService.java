package com.example.matdongsanserver.domain.dashboard.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.service.ChildService;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardErrorCode;
import com.example.matdongsanserver.domain.dashboard.exception.DashboardException;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.member.service.MemberService;
import com.example.matdongsanserver.domain.dashboard.dto.DashboardDto;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.dashboard.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.dashboard.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import com.example.matdongsanserver.domain.library.service.LibraryService;
import com.example.matdongsanserver.domain.story.service.StoryService;
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
    private final StoryLikeRepository storyLikeRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final LibraryService libraryService;
    private final MemberService memberService;
    private final ChildService childService;
    private final StoryRepository storyRepository;
    private final StoryService storyService;

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
    public List<StoryDto.QnAs> getQnaDetail(Long qnaId) {

        StoryQuestion storyQuestion = storyQuestionRepository.findByIdOrThrow(qnaId);

        return storyQuestion.getQuestionAnswers().stream()
                .map(StoryDto.QnAs::new)
                .toList();
    }
}
