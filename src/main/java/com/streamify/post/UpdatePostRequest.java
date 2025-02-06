package com.streamify.post;

import com.streamify.user.User;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {
    private String caption;
    private PostVisibility visibility;
    private String location;
    private Set<User> collaborators;
}
