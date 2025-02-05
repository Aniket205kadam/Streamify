package com.streamify.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {
    private final Logger LOGGER = LoggerFactory.getLogger(MediaServiceImpl.class);

    @Value("${application.file.upload.content-base-url}")
    private String fileUploadPath;

    @Override
    public String uploadPostContent(MultipartFile sourceFile, String userId, String postId) throws IOException {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        final String finalFileUploadPath = fileUploadPath + File.separator + userId + File.separator + postId;
        File targetFolder = new File(finalFileUploadPath);
        if (!targetFolder.exists()) {
            boolean isFolderCreated = targetFolder.mkdirs();
            if (!isFolderCreated) {
                LOGGER.error("Failed to create the target folder");
                throw new IllegalStateException("Failed to create the target folder");
            }
            LOGGER.info("Successfully create the target folder");
        }
        if (fileExtension == null) {
            throw new IllegalStateException("File extension is not supported");
        }
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            LOGGER.info("Post Content saved to {}", targetFilePath);
            return targetFilePath;
        } catch (IOException exception) {
            LOGGER.error("Post Content was not saved @error: {}", exception.getMessage());
            throw exception;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIdx = filename.lastIndexOf(".");
        if (lastDotIdx == -1) {
            return null;
        }
        return filename.substring(lastDotIdx + 1).toLowerCase();
    }
}
