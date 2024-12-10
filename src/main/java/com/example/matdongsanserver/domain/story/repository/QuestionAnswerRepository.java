package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
}
