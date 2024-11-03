package com.example.matdongsanserver.domain.member.repository;

import com.example.matdongsanserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
