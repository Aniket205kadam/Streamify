package com.streamify.comment;

import com.streamify.post.Post;
import com.streamify.post.PostRepository;
import com.streamify.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    private Post findPostById(@NonNull String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("The post is not found with ID: " + postId));
    }

    public String sendCommentOnPost(String postId, String content, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .status(CommentStatus.ACTIVE)
                .build();
        return commentRepository.save(comment).getId();
    }

    private Comment findCommentById(@NonNull String commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment is not found with ID: " + commentId));
    }

    public String updateComment(String commentId, Authentication connectedUser, String content) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        if (!(comment.getUser().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("You don't have the authority to delete the comment");
        }
        comment.setContent(content);
        return commentRepository.save(comment).getId();
    }

    public String likeComment(String commentId) {
        Comment comment = findCommentById(commentId);
        comment.setLikes(comment.getLikes() + 1);
        return commentRepository.save(comment).getId();
    }

    public String unlikeComment(String commentId) {
        Comment comment = findCommentById(commentId);
        comment.setLikes(comment.getLikes() - 1);
        return commentRepository.save(comment).getId();
    }

    public Boolean deleteComment(String commentId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You don't have to authority to delete the comment");
        }
        commentRepository.deleteById(comment.getId());
        return true;
    }

    public String sendReplyToComment(String postId, String commentId, String content, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        Comment reply = Comment.builder()
                .content(content)
                .user(user)
                .parentComment(comment)
                .status(CommentStatus.ACTIVE)
                .build();
        commentRepository.save(reply);
        if (!comment.getReplies().isEmpty()) {
            List<Comment> replies = comment.getReplies();
            replies.add(reply);
        }
        comment.setReplies(List.of(reply));
        return commentRepository.save(comment).getId();
    }
}
