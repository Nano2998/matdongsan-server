package com.example.matdongsanserver.domain.child.service;

import com.example.matdongsanserver.domain.child.dto.ChildDto;
import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.child.exception.ChildErrorCode;
import com.example.matdongsanserver.domain.child.exception.ChildException;
import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildService {

    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;

    /**
     * 자녀 추가 -> 전체 자녀를 반환
     * @param memberId
     * @param childRequest
     * @return
     */
    @Transactional
    public List<ChildDto.ChildDetail> registerChild(Long memberId, ChildDto.ChildRequest childRequest) {
        Member member = memberRepository.findByIdOrThrow(memberId);

        // 나이 검사
        validateChildAge(childRequest);

        childRepository.save(Child.builder()
                .member(member)
                .name(childRequest.getName())
                .englishAge(childRequest.getEnglishAge())
                .koreanAge(childRequest.getKoreanAge())
                .build());

        return childRepository.findByMemberId(memberId)
                .stream()
                .map(ChildDto.ChildDetail::new)
                .toList();
    }

    /**
     * 자녀의 나이 검사
     * @param childRequest
     */
    private static void validateChildAge(ChildDto.ChildRequest childRequest) {
        if (childRequest.getEnglishAge() < 3 || childRequest.getEnglishAge() > 8 ||
                childRequest.getKoreanAge() < 3 || childRequest.getKoreanAge() > 8) {
            throw new ChildException(ChildErrorCode.INVALID_AGE);
        }
    }

    /**
     * 자녀 조회
     * @param memberId
     * @return
     */
    public List<ChildDto.ChildDetail> getChildDetails(Long memberId) {
        return childRepository.findByMemberId(memberId)
                .stream()
                .map(ChildDto.ChildDetail::new)
                .toList();
    }

    /**
     * 자녀 정보 수정
     * @param memberId
     * @param childId
     * @param childRequest
     * @return
     */
    @Transactional
    public List<ChildDto.ChildDetail> updateChild(Long memberId, Long childId, ChildDto.ChildRequest childRequest) {
        Child child = childRepository.findByIdOrThrow(childId);
        Member member = memberRepository.findByIdOrThrow(memberId);

        if(!member.getChildren().contains(child)) {
            throw new ChildException(ChildErrorCode.CANNOT_ACCESS_CHILD);
        }

        // 나이 검사
        validateChildAge(childRequest);

        child.updateChild(childRequest.getName(), childRequest.getEnglishAge(), childRequest.getKoreanAge());
        return childRepository.findByMemberId(memberId)
                .stream()
                .map(ChildDto.ChildDetail::new)
                .toList();
    }

    /**
     * 자녀 삭제
     * @param memberId
     * @param childId
     */
    @Transactional
    public void deleteChild(Long memberId, Long childId) {
        Child child = childRepository.findByIdOrThrow(childId);
        Member member = memberRepository.findByIdOrThrow(memberId);

        if(!member.getChildren().contains(child)) {
            throw new ChildException(ChildErrorCode.CANNOT_ACCESS_CHILD);
        }
        member.removeChild(child);
        childRepository.delete(child);
    }
}
