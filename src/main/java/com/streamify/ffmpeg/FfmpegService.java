package com.streamify.ffmpeg;

import com.streamify.post.Post;
import com.streamify.post.PostMedia;
import com.streamify.post.PostMediaRepository;
import com.streamify.post.PostRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FfmpegService {
    private final Logger LOGGER = LoggerFactory.getLogger(FfmpegService.class);

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    @Value("${application.file.upload.content-base-url.post}")
    private String postBaseUrl;

    public FfmpegService(PostRepository postRepository, PostMediaRepository postMediaRepository) {
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
    }

    @PostConstruct
    public void init() {
        postBaseUrl = postBaseUrl.replace("/", File.separator);
    }

    @Async
    public void processVideoWithFfmpeg(Path fileUrl, String postId, String userId) throws IOException {
        final String finalFileUploadPath = postBaseUrl + File.separator + userId + File.separator + postId;
        File targetFolder = new File(finalFileUploadPath);
        if (!targetFolder.exists()) {
            boolean isFolderCreated = targetFolder.mkdirs();
            if (!isFolderCreated) {
                LOGGER.error("Failed to create the target folder");
                throw new IllegalStateException("Failed to create the target folder");
            }
            LOGGER.info("Successfully create the target folder");
        }
        String targetFilePath = finalFileUploadPath + File.separator + UUID.randomUUID();
        Path targetPath = Paths.get(targetFilePath);
        // create the target folder for ffmpeg
        boolean isTargetFolderCreated = new File(targetFilePath).mkdirs();
        if (!isTargetFolderCreated) {
            throw new IllegalStateException("Target folder for the ffmpeg is failed to create!");
        }
        try {
            Process process = getProcess(fileUrl, targetPath);
            int status = process.waitFor();
            LOGGER.info("Process: status is: {}", status);
            ProcessHandle.Info info = process.info();
            LOGGER.info(info.toString());
            if (status != 0) {
                throw new Exception("Failed: to process the video!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Post is not found with ID: " + postId)
                );
        PostMedia postMedia = post.getPostMedia().stream()
                .filter(media ->
                        media.getMediaUrl().equals(fileUrl.toString())
                )
                .findAny()
                .orElseThrow(() ->
                        new EntityNotFoundException("PostMedia is not found with URL: " + fileUrl.toString())
                );
        postMedia.setMediaUrl(targetPath.toString());
        postMediaRepository.save(postMedia);

        // remove the temp post video from the location
        Files.delete(fileUrl);
        LOGGER.info("Post Video Processing is done");
    }

    private static Process getProcess(Path fileUrl, Path targetPath) throws IOException {
        String ffmpegCmd = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                fileUrl,
                targetPath,
                targetPath
        );
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

}
