package com.streamify.user;

import com.streamify.common.PageResponse;
import com.streamify.post.PostResponse;
import com.streamify.post.PostService;
import com.streamify.story.StoryResponse;
import com.streamify.story.StoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@Tag(name = "User")
public class UserController {
    private final PostService postService;
    private final StoryService storyService;

    public UserController(PostService postService, StoryService storyService) {
        this.postService = postService;
        this.storyService = storyService;
    }

    @GetMapping("/{user-id}/posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllPostByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllPostsByUserId(page, size, userId));
    }

    @GetMapping("/{user-id}/reels")
    public ResponseEntity<PageResponse<PostResponse>> getAllReelsByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllReelsByUserId(page, size, userId));
    }

    @GetMapping("/{user-id}/saved-posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllSavedPostsByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllSavedPostsByUser(page, size, userId));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllMyPost(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllMyPost(page, size, connectedUser));
    }

    @GetMapping("/{user-id}/stories")
    public ResponseEntity<List<StoryResponse>> findStoryByUser(
            @PathVariable("user-id") String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storyService.findStoriesByUser(userId));
    }
}
