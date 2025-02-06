package com.streamify.post;

import com.streamify.comment.CommentRepository;
import com.streamify.common.PageResponse;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final PostMediaRepository postMediaRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, MediaServiceImpl mediaService, PostMediaRepository postMediaRepository, PostMapper postMapper, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.mediaService = mediaService;
        this.postMediaRepository = postMediaRepository;
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
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

        // check if this is reels or not
        if (request.getFiles().size() == 1 && Objects.requireNonNull(request.getFiles().getFirst().getFile().getContentType()).startsWith("video/")) {
            post.setReel(true);
        }

        // save the post
        Post savedPost = postRepository.save(post);

        // save the post content
        for (FileMetadata content : request.getFiles()) {
            if (Objects.requireNonNull(content.getFile().getContentType()).startsWith("image/")) {
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
        return savedPost.getId();
    }

    private String generateAltTextForVideo(MultipartFile file) {
        // todo -> Integrate AI and generate the alt text for the image
        return null;
    }

    private String generateAltTextForImage(MultipartFile file) {
        // todo -> Integrate AI and generate the alt text for the video
        return null;
    }

    private Post findPostById(@NonNull String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("The post is not found with ID: " + postId));
    }

    public String updatePost(String postId, UpdatePostRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        if (!(post.getUser().getId().equals(user.getId()))) {
            throw new IllegalStateException("You have not authority to update this post!");
        }
        post.setCaption(request.getCaption());
        post.setVisibility(request.getVisibility());
        post.setLocation(request.getLocation());
        post.setCollaborators(request.getCollaborators());
        return postRepository.save(post).getId();
    }

    public String updateHideLikeCount(String postId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        if (!(post.getUser().getId().equals(user.getId()))) {
            throw new IllegalStateException("You have not authority to update this post!");
        }
        post.setHideLikesAndViewCounts(!post.isHideLikesAndViewCounts());
        return postRepository.save(post).getId();
    }

    public String updateCommenting(String postId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        if (!(post.getUser().getId().equals(user.getId()))) {
            throw new IllegalStateException("You have not authority to update this post!");
        }
        post.setAllowComments(!post.isAllowComments());
        return postRepository.save(post).getId();
    }

    public PostResponse getPostById(String postId) {
         return postRepository.findById(postId)
                    .map(postMapper::toPostResponse)
                    .orElseThrow(() ->
                            new EntityNotFoundException("The post is not found with ID: " + postId)
                    );
    }

    private User findUserById(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("This user is not found with ID: " + userId));
    }

    public PageResponse<PostResponse> getAllPostsByUserId(int page, int size, String userId) {
        User user = findUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllDisplayablePosts(pageable, user.getId());
        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        return PageResponse.<PostResponse>builder()
                .content(postResponses)
                .number(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    public PageResponse<PostResponse> getAllReelsByUserId(int page, int size, String userId) {
        User user = findUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllDisplayableReels(pageable, user.getId());
        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        return PageResponse.<PostResponse>builder()
                .content(postResponses)
                .number(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    public PageResponse<PostResponse> getAllSavedPostsByUser(int page, int size, String userId) {
        User user = findUserById(userId);
        List<String> savedPostIds = user.getSavedPost()
                .stream()
                .map(Post::getId)
                .toList();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllMySavedPosts(pageable, savedPostIds);
        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        return PageResponse.<PostResponse>builder()
                .content(postResponses)
                .number(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    public PageResponse<PostResponse> getAllTaggedPosts(int page, int size, String userId) {
        User user = findUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllTaggedPosts(pageable, user.getId());
        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        return PageResponse.<PostResponse>builder()
                .content(postResponses)
                .number(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    public PageResponse<PostResponse> getAllMyPost(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllMyPosts(pageable, user.getId());
        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
        return PageResponse.<PostResponse>builder()
                .content(postResponses)
                .number(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    public Boolean deletePostById(String postId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        if (!(post.getUser().getId().equals(user.getId()))) {
            throw new IllegalStateException("You don't have the authority to delete the post!");
        }
        postRepository.deleteById(post.getId());
        return true;
    }
}
