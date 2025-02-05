package com.streamify.post;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @NotNull(message = "File is mandatory")
    private MultipartFile file;
    private List<MediaTag> tags;
    private String altText;
}
