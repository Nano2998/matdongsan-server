package com.example.matdongsanserver.domain.child.repository;

import com.example.matdongsanserver.domain.child.entity.Child;
import com.example.matdongsanserver.domain.child.exception.ChildErrorCode;
import com.example.matdongsanserver.domain.child.exception.ChildException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    default Child findByIdOrThrow(Long childId) {
        return findById(childId).orElseThrow(
                () -> new ChildException(ChildErrorCode.CHILD_NOT_FOUND)
        );
    }
    List<Child> findByMemberId(Long memberId);
}
