package com.streamify.story;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {
    @Query("""
            SELECT story
            FROM Story story
            WHERE story.id = :storyId
            AND story.isArchived = false
            AND story.expiredAt > CURRENT_TIMESTAMP
            """)
    Optional<Story> findValidStoryById(@Param("storyId") String storyId);

    @Query("""
            SELECT story
            FROM Story story
            WHERE story.user.id = :userId
            AND story.isArchived = false
            AND story.expiredAt > CURRENT_TIMESTAMP
            ORDER BY story.createdAt ASC
            """)
    List<Story> findAllValidUserStories(@Param("userId") String userId);
}
