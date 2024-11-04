package com.example.matdongsanserver.domain.member.repository;

import com.example.matdongsanserver.domain.member.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByMemberId(Long memberId);
}
