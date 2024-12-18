package com.example.matdongsanserver.domain.story.service;

import com.example.matdongsanserver.domain.member.dto.MemberDto;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.exception.MemberErrorCode;
import com.example.matdongsanserver.domain.member.exception.MemberException;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
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
    @DisplayName("모든 QnA 로그 가져오기 성공")
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

        // 5. 동화 질문 및 답변 생성하기
        StoryQuestion question1 = createQuestionWithAnswer(child, storyId, "질문 내용", "샘플 답변", "테스트코드 답변");
        StoryQuestion question2 = createQuestionWithAnswer(child, storyId, "질문 내용2", "샘플 답변2", "테스트코드 답변2");
        storyQuestionRepository.saveAll(List.of(question1, question2));

        // When
        Page<ParentDto.ParentQnaLogRequest> qnaLog = parentService.getQnaLog(member.getId(), Pageable.unpaged());

        // Then
        assertThat(qnaLog).isNotNull();
        assertThat(qnaLog.getContent()).hasSize(2); // 사이즈 2 검증
    }

    @Test
    @DisplayName("자녀 QnA 모두보기")
    void getChildQna() {
        // Given
        // 1. 회원 생성
        Member member = createAndSaveMember();

        // 2. 동화 제작
        createStory(member.getNickname(), member.getId(), "테스트 제목", Language.KO, 4);

        // 3. 자녀 등록
        MemberDto.ChildRequest childRequest = createChildRequest("테스트", 4, 4);
        MemberDto.ChildRequest childRequest2 = createChildRequest("테스트2", 3, 3);
        memberService.registerChild(member.getId(), childRequest);
        memberService.registerChild(member.getId(), childRequest2);
        List<MemberDto.ChildDetail> childDetails = memberService.getChildDetails(member.getId());
        Long childId = childDetails.get(0).getId();
        Long childId2 = childDetails.get(1).getId();

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.CHILD_NOT_FOUND));

        Child child2= childRepository.findById(childId2)
                .orElseThrow(() -> new MemberException(MemberErrorCode.CHILD_NOT_FOUND));

        // 4. 스토리 id 가져오기
        Page<StoryDto.StorySummary> stories = libraryService.getStories(AgeType.LV1, LangType.KO, SortType.RECENT, Pageable.unpaged());
        String storyId = stories.getContent().get(0).getId();

        // 5. 동화 질문 및 답변 생성
        StoryQuestion question1 = createQuestionWithAnswer(child, storyId, "질문 내용", "샘플 답변", "테스트코드 답변");
        StoryQuestion question2 = createQuestionWithAnswer(child2, storyId, "질문 내용2", "샘플 답변2", "테스트코드 답변2");
        StoryQuestion question3 = createQuestionWithAnswer(child2, storyId, "질문 내용3", "샘플 답변3", "테스트코드 답변2");

        storyQuestionRepository.saveAll(List.of(question1, question2, question3));

        // When
        Page<ParentDto.ParentQnaLogRequest> qnaLog = parentService.getChildQnaLog(child2.getId(), Pageable.unpaged());

        // Then
        assertThat(qnaLog).isNotNull();
        assertThat(qnaLog.getContent()).hasSize(2); // 사이즈 2 검증
    }

    @Test
    @DisplayName("QnA 상세보기")
    void getQnaDetail() {
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

        // 5. 동화 질문 및 답변 생성
        StoryQuestion question = createQuestionWithAnswer(child, storyId, "질문 내용", "샘플 답변", "테스트코드 답변");
        StoryQuestion save = storyQuestionRepository.save(question);
        // When
        List<StoryDto.QnAs> qnaDetail = parentService.getQnaDetail(save.getId());

        // Then
        assertThat(qnaDetail.get(0).getQuestion()).isEqualTo("질문 내용");
        assertThat(qnaDetail.get(0).getSampleAnswer()).isEqualTo("샘플 답변");
        assertThat(qnaDetail.get(0).getAnswer()).isEqualTo("테스트코드 답변");
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

    // 동화 생성 및 답변 등록하기
    private StoryQuestion createQuestionWithAnswer(Child child, String storyId, String questionText, String sampleAnswer, String finalAnswer) {
        StoryQuestion storyQuestion = StoryQuestion.builder()
                .language(Language.KO)
                .storyId(storyId)
                .build();

        QuestionAnswer qa = QuestionAnswer.builder()
                .question(questionText)
                .sampleAnswer(sampleAnswer)
                .storyQuestion(storyQuestion)
                .build();
        qa.updateAnswer(finalAnswer);

        storyQuestion.updateChild(child);

        return storyQuestion;
    }
}