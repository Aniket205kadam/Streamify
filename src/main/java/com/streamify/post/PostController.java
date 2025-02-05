package com.streamify.post;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("posts")
@Tag(name = "Post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    public ResponseEntity<?> uploadPost(
            @RequestBody @Valid UploadPostRequest request,
            Authentication connectedUser
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.uploadPost(request, connectedUser));
    }
}
