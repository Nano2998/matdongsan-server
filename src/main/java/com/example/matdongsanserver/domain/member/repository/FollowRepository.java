package com.example.matdongsanserver.domain.member.repository;

import com.example.matdongsanserver.domain.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowingIdAndFollowerId(Long followingId, Long follwerId);
}
