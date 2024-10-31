package com.example.matdongsanserver.domain.story.repository;

import com.example.matdongsanserver.domain.story.document.Story;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoryRepository extends MongoRepository<Story, String> {
}
