package com.streamify.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {
    private String id;
    private String username;
    private String avtarUrl;
}
