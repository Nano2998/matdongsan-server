package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.member.service.MemberService;
import com.example.matdongsanserver.domain.story.dto.ParentDto;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.story.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.story.repository.StoryLikeRepository;
import com.example.matdongsanserver.domain.story.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentService {

    private final StoryQuestionRepository storyQuestionRepository;
    private final MemberRepository memberRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final LibraryService libraryService;
    private final MemberService memberService;
    private final StoryRepository storyRepository;
    private final StoryService storyService;

    /**
     * 부모 qna로그 전체 가져오기
     * @param memberId
     * @return
     */
    public Page<ParentDto.ParentQnaLogRequest> getQnaLog(Long memberId, Pageable pageable) {
        if(!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        List<MemberDto.ChildDetail> childDetails = memberService.getChildDetails(memberId);

        List<Long> childIds = childDetails.stream()
                .map(MemberDto.ChildDetail::getId)
                .toList();

        Page<StoryQuestion> childQuestions = storyQuestionRepository.findAllByChildIdIn(childIds, pageable);

        List<String> storyIds = childQuestions.getContent().stream()
                .map(StoryQuestion::getStoryId)
                .distinct()
                .toList();

        List<Story> stories = storyRepository.findByIdIn(storyIds);

        Map<String, String> storyTitleMap = stories.stream()
                .collect(Collectors.toMap(Story::getId, Story::getTitle));

        return childQuestions.map(question -> ParentDto.ParentQnaLogRequest.builder()
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
    public Page<ParentDto.ParentQnaLogRequest> getChildQnaLog(Long childId, Pageable pageable) {
        Page<StoryQuestion> childQuestions = storyQuestionRepository.findByChildId(childId, pageable);

        List<String> storyIds = childQuestions.getContent().stream()
                .map(StoryQuestion::getStoryId)
                .distinct()
                .toList();

        List<Story> stories = storyRepository.findByIdIn(storyIds);

        Map<String, String> storyTitleMap = stories.stream()
                .collect(Collectors.toMap(Story::getId, Story::getTitle));

        return childQuestions.map(question -> ParentDto.ParentQnaLogRequest.builder()
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

        StoryQuestion storyQuestion = storyQuestionRepository.findById(qnaId)
                .orElseThrow(() -> new StoryException(StoryErrorCode.STORY_QUESTION_NOT_FOUND));

        return storyQuestion.getQuestionAnswers().stream()
                .map(StoryDto.QnAs::new)
                .toList();
    }


}
