package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.member.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.member.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import com.example.matdongsanserver.domain.member.service.MemberService;
import com.example.matdongsanserver.domain.module.service.ModuleService;
import com.example.matdongsanserver.domain.story.AgeType;
import com.example.matdongsanserver.domain.story.LangType;
import com.example.matdongsanserver.domain.story.SortType;
import com.example.matdongsanserver.domain.story.dto.ParentDto;
import com.example.matdongsanserver.domain.story.dto.StoryDto;
import com.example.matdongsanserver.domain.story.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.story.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.entity.mongo.Language;
import com.example.matdongsanserver.domain.story.entity.mongo.Story;
import com.example.matdongsanserver.domain.story.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.repository.mongo.StoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayName("대시보드 서비스 테스트")
class ParentServiceTest {

    @Autowired
    StoryQuestionRepository storyQuestionRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StoryRepository storyRepository;
    @Autowired
    StoryService storyService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    ChildRepository childRepository;
    @Autowired
    ParentService parentService;
    @Autowired
    ModuleService moduleService;

    @BeforeEach
    void setUp() {
        storyQuestionRepository.deleteAll();
        storyRepository.deleteAll();
        memberRepository.deleteAll();
        childRepository.deleteAll();

    }

    @Test
    @DisplayName("QnA 로그 가져오기 성공")
    void getQnaLog() {
        // Given
        // 1. 회원 생성
        Member member = createAndSaveMember();

        // 2. 동화 제작
        createStory(member.getNickname(), member.getId(), "테스트 제목", Language.KO, 4);

        // 3. 자녀 등록
        MemberDto.ChildRequest childRequest = createChildRequest("테스트", 4, 4);
        memberService.registerChild(member.getId(), childRequest);
        List<MemberDto.ChildDetail> childDetails = memberService.getChildDetails(member.getId());
        Long childId = childDetails.get(0).getId();

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.CHILD_NOT_FOUND));

        // 4. 스토리 id 가져오기
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.KO, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();

        // 5. 첫 번째 동화 질문 생성
        StoryQuestion storyQuestion = StoryQuestion.builder()
                .language(Language.KO)
                .storyId(storyId)
                .build();

        // 6. 첫 번째 질문 답변 추가
        QuestionAnswer qa = QuestionAnswer.builder()
                .question("질문 내용")
                .sampleAnswer("샘플 답변")
                .storyQuestion(storyQuestion)
                .build();
        qa.updateAnswer("테스트코드 답변");

        storyQuestion.updateChild(child);

        // 7. 두 번째 동화 질문 생성
        StoryQuestion storyQuestion2 = StoryQuestion.builder()
                .language(Language.KO)
                .storyId(storyId)
                .build();

        // 8. 두 번째 질문 답변 추가
        QuestionAnswer qa2 = QuestionAnswer.builder()
                .question("질문 내용2")
                .sampleAnswer("샘플 답변2")
                .storyQuestion(storyQuestion2)
                .build();
        qa2.updateAnswer("테스트코드 답변2");

        storyQuestion2.updateChild(child);

        storyQuestionRepository.save(storyQuestion);
        storyQuestionRepository.save(storyQuestion2);

        // When
        Page<ParentDto.ParentQnaLogRequest> qnaLog = parentService.getQnaLog(member.getId(), Pageable.unpaged());

        // Then
        assertThat(qnaLog).isNotNull();
        assertThat(qnaLog.getContent()).hasSize(2); // 사이즈 2 검증
    }

    // 멤버 생성
    private Member createAndSaveMember() {
        Member member = Member.builder()
                .email("test@naver.com")
                .nickname("test")
                .profileImage("testImg")
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
    // 자녀 추가
    private MemberDto.ChildRequest createChildRequest(String name, int englishAge, int koreanAge) {
        return MemberDto.ChildRequest.builder()
                .name(name)
                .englishAge(englishAge)
                .koreanAge(koreanAge)
                .build();
    }
}