package com.streamify.post;

import com.streamify.comment.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
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

    @PatchMapping("/{post-id}/like")
    public ResponseEntity<String> likePost(
            @PathVariable("post-id") String postId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.likePost(postId));
    }

    @PostMapping("/{post-id}/comment")
    public ResponseEntity<String> sendCommentOnPost(
            @PathVariable("post-id") String postId,
            @RequestParam("content") @Size(max = 22000, message = "Comment must not exceed 2200 characters")
            String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.sendCommentOnPost(postId, content, connectedUser));
    }

    @PatchMapping("/comment/{comment-id}")
    public ResponseEntity<String> updateComment(
            @PathVariable("comment-id") String commentId,
            @RequestParam("content") @Size(max = 22000, message = "Comment must not exceed 2200 characters")
            String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.updateComment(commentId, connectedUser, content));
    }

    @PatchMapping("/comment/{comment-id}/like")
    public ResponseEntity<String> likeComment(
            @PathVariable("comment-id") String commentId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.likeComment(commentId));
    }

    @PatchMapping("/comment/{comment-id}/unlike")
    public ResponseEntity<String> unlikeComment(
            @PathVariable("comment-id") String commentId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.unlikeComment(commentId));
    }

    @DeleteMapping("/comment/{comment-id}")
    public ResponseEntity<Boolean> deleteCommentById(
            @PathVariable("comment-id") String commentId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.deleteComment(commentId, connectedUser));
    }

    @PostMapping("/{post-id}/comment/{comment-id}/reply")
    public ResponseEntity<String> sendReplyToComment(
            @PathVariable("post-id") String postId,
            @PathVariable("comment-id") String commentId,
            @RequestParam("content") String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.sendReplyToComment(postId, commentId, content, connectedUser));
    }
}
