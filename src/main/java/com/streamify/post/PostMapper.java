package com.streamify.post;

import org.springframework.stereotype.Service;

@Service
public class PostMapper {

    public PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .visibility(post.getVisibility())
                .isArchived(post.isArchived())
                .location(post.getLocation())
                .isReel(post.isReel())
                .collaborators(post.getCollaborators())
                .hideLikesAndViewCounts(post.isHideLikesAndViewCounts())
                .allowComments(post.isAllowComments())
                .comments(post.getComments())
                .postMedia(post.getPostMedia())
                .build();
    }
}
