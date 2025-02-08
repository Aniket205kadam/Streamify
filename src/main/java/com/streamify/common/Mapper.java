package com.streamify.common;

import com.streamify.comment.Comment;
import com.streamify.comment.CommentResponse;
import org.springframework.stereotype.Service;

@Service
public class Mapper {
    public CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .likes(comment.getLikes())
                .replies(comment.getReplies().size())
                .build();
    }

    public CommentResponse toCommentRelyResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .likes(comment.getLikes())
                .build();
    }
}
