package com.streamify.post;

import com.streamify.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final PostMediaRepository postMediaRepository;

    public PostService(PostRepository postRepository, MediaServiceImpl mediaService, PostMediaRepository postMediaRepository) {
        this.postRepository = postRepository;
        this.mediaService = mediaService;
        this.postMediaRepository = postMediaRepository;
    }

    public String uploadPost(
            UploadPostRequest request,
            Authentication connectedUser
    ) throws IOException {
        User user = (User) connectedUser.getPrincipal();

        Post post = Post.builder()
                .caption(request.getCaption())
                .visibility(request.getVisibility())
                .isArchived(request.isArchived())
                .location(request.getLocation())
                .collaborators(request.getCollaborators())
                .hideLikesAndViewCounts(request.isHideLikesAndViewCounts())
                .allowComments(request.isAllowComments())
                .build();

        // save the post
        Post savedPost = postRepository.save(post);

        // check if this is reels or not
        if (request.getFiles().size() == 1 && Objects.requireNonNull(request.getFiles().getFirst().getFile().getContentType()).startsWith("video/")) {
            // todo -> stored as the post as well as stored as the reel
        }

        // save the post content
        for (FileMetadata content : request.getFiles()) {
            if (content.getFile().getContentType().startsWith("image/")) {
                String storedImageUrl = mediaService.uploadPostContent(content.getFile(), user.getId(), savedPost.getId());
                if (content.getAltText() == null) {
                    // generate altText by using AI
                    content.setAltText(generateAltTextForImage(content.getFile()));
                }
                PostMedia postMedia = PostMedia
                        .builder()
                        .post(savedPost)
                        .mediaUrl(storedImageUrl)
                        .type(content.getFile().getContentType())
                        .altText(content.getAltText())
                        .tags(content.getTags())
                        .build();
                postMediaRepository.save(postMedia);
            } else if (content.getFile().getContentType().startsWith("video/")) {
                String storedVideoUrl = mediaService.uploadPostContent(content.getFile(), user.getId(), savedPost.getId());
                if (content.getAltText() == null) {
                    // generate altText by AI
                    content.setAltText(generateAltTextForVideo(content.getFile()));
                }
                PostMedia postMedia = PostMedia
                        .builder()
                        .post(savedPost)
                        .mediaUrl(storedVideoUrl)
                        .type(content.getFile().getContentType())
                        .altText(content.getAltText())
                        .tags(content.getTags())
                        .build();
                postMediaRepository.save(postMedia);
            } else {
                throw new IllegalArgumentException("Only image and video file types are allowed. Please upload a valid image or video.");
            }
        }
        return null;
    }

    private String generateAltTextForVideo(MultipartFile file) {
        // todo -> Integrate AI and generate the alt text for the image
        return null;
    }

    private String generateAltTextForImage(MultipartFile file) {
        // todo -> Integrate AI and generate the alt text for the video
        return null;
    }
}
