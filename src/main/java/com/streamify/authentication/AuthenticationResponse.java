package com.streamify.authentication;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private LocalDateTime createdAt;
    private Date validateAt;
}
