package com.streamify.post;

import com.streamify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            AND post.isArchived = true
            AND post.visibility = PUBLIC
            """)
    Page<Post> findAllDisplayablePosts(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            AND post.isReel = true
            AND post.isArchived = true
            AND post.visibility = PUBLIC
            """)
    Page<Post> findAllDisplayableReels(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.postMedia.tags.taggeduser.id = :userId
            AND post.isArchived = true
            AND post.visibility = PUBLIC
            """)
    Page<Post> findAllTaggedPosts(Pageable pageable, @Param("user-id") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            """)
    Page<Post> findAllMyPosts(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.id IN :savedPostIds
            """)
    Page<Post> findAllMySavedPosts(Pageable pageable, @Param("savedPostIds") List<String> savedPostIds);
}
