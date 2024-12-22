package com.example.matdongsanserver.domain.follow.repository;

import com.example.matdongsanserver.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowingIdAndFollowerId(Long followingId, Long follwerId);
    List<Follow> findByFollowingId(Long followingId);
    boolean existsByFollowingIdAndFollowerId(Long followingId, Long followerId);
}
