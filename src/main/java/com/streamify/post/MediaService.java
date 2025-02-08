package com.streamify.post;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface MediaService {
    String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException;

    boolean deletePostContent(List<PostMedia> postMedias) throws IOException;
}
