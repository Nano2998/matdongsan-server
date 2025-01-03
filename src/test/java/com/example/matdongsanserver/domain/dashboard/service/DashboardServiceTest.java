package com.example.matdongsanserver.domain.dashboard.service;

import com.example.matdongsanserver.common.external.ExternalApiRequest;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.dashboard.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.dashboard.dto.DashboardDto;
import com.example.matdongsanserver.domain.dashboard.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.dashboard.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("대시보드 서비스 테스트")
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private StoryQuestionRepository storyQuestionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private QuestionAnswerRepository questionAnswerRepository;

    @Mock
    private ChildRepository childRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ExternalApiRequest externalApiRequest;

    @Test
    @DisplayName("동화 질문 생성 성공")
    void registerQuestions() {
        // Given
        String storyId = "story123";
        Story story = mock(Story.class);
        StoryQuestion storyQuestion = mock(StoryQuestion.class);

        List<Map<String, String>> parsedQuestions = List.of(
                Map.of("Q", "질문1", "A", "답변1"),
                Map.of("Q", "질문2", "A", "답변2")
        );

        when(storyRepository.findByIdOrThrow(storyId)).thenReturn(story);
        when(story.getLanguage()).thenReturn(Language.KO);
        when(story.getAge()).thenReturn(5);
        when(story.getContent()).thenReturn("동화 내용");
        when(storyQuestionRepository.save(any(StoryQuestion.class))).thenReturn(storyQuestion);
        when(externalApiRequest.sendQuestionRequest(any(), anyInt(), any())).thenReturn(parsedQuestions);

        // When
        DashboardDto.StoryQuestionResponse response = dashboardService.registerQuestions(storyId);

        // Then
        assertThat(response).isNotNull();
        verify(storyQuestionRepository, times(1)).save(any(StoryQuestion.class));
        verify(questionAnswerRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("부모 QnA 로그 조회 성공")
    void getQnaLog() {
        // Given
        Long memberId = 1L;
        Child child = mock(Child.class);
        StoryQuestion storyQuestion = mock(StoryQuestion.class);
        Story story = mock(Story.class);

        List<Child> children = List.of(child);
        Page<StoryQuestion> childQuestions = new PageImpl<>(List.of(storyQuestion));

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(childRepository.findByMemberId(memberId)).thenReturn(children);
        when(child.getId()).thenReturn(1L);
        when(storyQuestionRepository.findAllByChildIdIn(anyList(), any())).thenReturn(childQuestions);
        when(storyRepository.findByIdIn(anyList())).thenReturn(List.of(story));
        when(story.getId()).thenReturn("story123");
        when(story.getTitle()).thenReturn("테스트 동화");
        when(storyQuestion.getId()).thenReturn(1L);
        when(storyQuestion.getStoryId()).thenReturn("story123");
        when(storyQuestion.getChild()).thenReturn(child);
        when(child.getName()).thenReturn("테스트 자녀");

        // When
        Page<DashboardDto.ParentQnaLogResponse> result = dashboardService.getQnaLog(memberId, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 동화");
    }

    @Test
    @DisplayName("부모 QnA 로그 조회 실패 - 멤버 없음")
    void getQnaLog_Fail_MemberNotFound() {
        // Given
        Long memberId = 1L;
        when(memberRepository.existsById(memberId)).thenReturn(false);

        // When
        MemberException memberException = assertThrows(MemberException.class, () ->
                dashboardService.getQnaLog(memberId, Pageable.unpaged())
        );

        // Then
        assertThat(memberException.getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("자녀 QnA 로그 조회 성공")
    void getChildQnaLog() {
        // Given
        Long childId = 1L;
        StoryQuestion storyQuestion = mock(StoryQuestion.class);
        Story story = mock(Story.class);

        Page<StoryQuestion> childQuestions = new PageImpl<>(List.of(storyQuestion));

        when(storyQuestionRepository.findByChildId(childId, Pageable.unpaged())).thenReturn(childQuestions);
        when(storyRepository.findByIdIn(anyList())).thenReturn(List.of(story));
        when(story.getId()).thenReturn("story123");
        when(story.getTitle()).thenReturn("테스트 동화");
        when(storyQuestion.getId()).thenReturn(1L);
        when(storyQuestion.getStoryId()).thenReturn("story123");
        when(storyQuestion.getChild()).thenReturn(mock(Child.class));

        // When
        Page<DashboardDto.ParentQnaLogResponse> result = dashboardService.getChildQnaLog(childId, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("QnA 상세 조회 성공")
    void getQnaDetail() {
        // Given
        Long qnaId = 1L;
        StoryQuestion storyQuestion = mock(StoryQuestion.class);
        QuestionAnswer answer1 = mock(QuestionAnswer.class);
        QuestionAnswer answer2 = mock(QuestionAnswer.class);

        when(storyQuestionRepository.findByIdOrThrow(qnaId)).thenReturn(storyQuestion);
        when(storyQuestion.getQuestionAnswers()).thenReturn(List.of(answer1, answer2));
        when(answer1.getQuestion()).thenReturn("질문1");
        when(answer1.getSampleAnswer()).thenReturn("샘플 답변1");
        when(answer1.getAnswer()).thenReturn("최종 답변1");
        when(answer2.getQuestion()).thenReturn("질문2");
        when(answer2.getSampleAnswer()).thenReturn("샘플 답변2");
        when(answer2.getAnswer()).thenReturn("최종 답변2");

        // When
        List<DashboardDto.QnAs> result = dashboardService.getQnaDetail(qnaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestion()).isEqualTo("질문1");
    }
}