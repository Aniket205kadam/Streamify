package com.streamify.post;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("posts")
@Tag(name = "Post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<String> uploadPost(
            @RequestBody @Valid UploadPostRequest request,
            Authentication connectedUser
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.uploadPost(request, connectedUser));
    }

    @PutMapping("/{post-id}")
    public ResponseEntity<String> updatePost(
            @RequestBody @Valid UpdatePostRequest request,
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updatePost(postId, request, connectedUser));
    }

    @PatchMapping("/{post-id}/hide/like-count")
    public ResponseEntity<String> updateHideLikeCount(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updateHideLikeCount(postId, connectedUser));
    }

    @PatchMapping("/{post-id}/hide/commenting")
    public ResponseEntity<String> updateCommenting(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updateCommenting(postId, connectedUser));
    }

    @GetMapping("/{post-id}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable("post-id") String postId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getPostById(postId));
    }

    @DeleteMapping("/{post-id}")
    public ResponseEntity<Boolean> deletePostById(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.deletePostById(postId, connectedUser));
    }
}
