package com.streamify.post;

import com.streamify.user.User;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadPostRequest {
    private String caption;
    private PostVisibility visibility;
    private boolean isArchived;
    private String location;
    private Set<User> collaborators;
    private boolean hideLikesAndViewCounts;
    private boolean allowComments;
    private List<FileMetadata> files;
}
