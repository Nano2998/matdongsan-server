package com.example.matdongsanserver.domain.dashboard.repository;

import com.example.matdongsanserver.domain.dashboard.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
}
