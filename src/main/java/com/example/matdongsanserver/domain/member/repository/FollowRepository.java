package com.example.matdongsanserver.domain.member.repository;

import com.example.matdongsanserver.domain.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
