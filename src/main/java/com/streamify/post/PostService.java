package com.streamify.post;

import com.streamify.Storage.MediaService;
import com.streamify.Storage.MediaServiceImpl;
import com.streamify.comment.CommentRepository;
import com.streamify.common.PageResponse;
import com.streamify.ffmpeg.FfmpegService;
import com.streamify.user.User;
import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final PostMediaRepository postMediaRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final FfmpegService ffmpegService;

    public PostService(
            PostRepository postRepository,
            MediaServiceImpl mediaService,
            PostMediaRepository postMediaRepository,
            PostMapper postMapper,
            UserRepository userRepository,
            CommentRepository commentRepository, FfmpegService ffmpegService
    ) {
        this.postRepository = postRepository;
        this.mediaService = mediaService;
        this.postMediaRepository = postMediaRepository;
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.ffmpegService = ffmpegService;
    }

    private Post findPostById(@NonNull String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("The post is not found with ID: " + postId));
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
        Page<Post> posts = postRepository.findAllDisplayablePosts(pageable, user.getId(), PostVisibility.PUBLIC);
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
        Page<Post> posts = postRepository.findAllDisplayableReels(pageable, user.getId(), PostVisibility.PUBLIC);
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

    public Boolean deletePostById(String postId, Authentication connectedUser) throws IOException {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        if (!(post.getUser().getId().equals(user.getId()))) {
            throw new IllegalStateException("You don't have the authority to delete the post!");
        }
        try {
            boolean isContentDeleted = mediaService.deletePostContent(post.getPostMedia());
            if (!isContentDeleted)
                throw new IOException("Files deletion failed!");
        } catch (IOException exception) {
            throw new IllegalStateException("Post deletion failed!");
        }
        postRepository.deleteById(post.getId());
        return true;
    }

    public String likePost(String postId) {
        return null;
    }

    public String uploadPostContent(Authentication connectedUser, MultipartFile... contents) throws IOException {
        User user = (User) connectedUser.getPrincipal();
        Post post = Post.builder()
                .visibility(PostVisibility.PUBLIC)
                .isArchived(true)
                .hideLikesAndViewCounts(false)
                .allowComments(true)
                .user(user)
                .build();
        Post savedPost = postRepository.save(post);

        List<PostMedia> postMediaList = new ArrayList<>();
        for (MultipartFile content : contents) {
            if (content.getContentType().startsWith("image/") || content.getContentType().startsWith("video/")) {
                String storedUrl = mediaService.uploadPostContent(content, user.getId(), savedPost.getId());
                PostMedia postMedia = PostMedia.builder()
                        .post(savedPost)
                        .mediaUrl(storedUrl)
                        .type(content.getContentType())
                        .altText(generateAltText(content))
                        .build();
                PostMedia savedPostMedia = postMediaRepository.save(postMedia);
                postMediaList.add(savedPostMedia);
                // check this is reel or not
                if (contents.length == 1 && content.getContentType().startsWith("video/")) {
                    ffmpegService.isValidReel(Paths.get(storedUrl), savedPost.getId());
                }
            } else {
                throw new IllegalArgumentException("Only image and video file types are allowed. Please upload a valid image or video.");
            }
        }
        savedPost.setPostMedia(postMediaList);
        return postRepository.save(savedPost).getId();
    }

    private String generateAltText(MultipartFile content) {
        // todo -> generate the text by using the AI
        return "null";
    }

    @Transactional
    public String uploadPostMetaData(PostRequest request, String postId, Authentication connectedUser) {
        Post post = findPostById(postId);
        // override post meta-data
        post.setCaption(request.getCaption());
        post.setVisibility(request.getVisibility());
        post.setArchived(request.isArchived());
        post.setLocation(request.getLocation());
        post.setCollaborators(
                new HashSet<>(userRepository.findAllById(request.getCollaborators()))
        );
        post.setHideLikesAndViewCounts(request.isHideLikesAndViewCounts());
        post.setAllowComments(request.isAllowComments());
        postRepository.save(post);
        return postRepository.save(post).getId();
    }

    @Transactional
    public PostResponse updatePost(PostRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(request.getId());
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You don't have the permission to delete the post");
        }
        post.setCaption(request.getCaption());
        post.setVisibility(request.getVisibility());
        post.setArchived(request.isArchived());
        post.setLocation(request.getLocation());
        post.setCollaborators(
                new HashSet<>(userRepository.findAllById(request.getCollaborators()))
        );
        post.setHideLikesAndViewCounts(request.isHideLikesAndViewCounts());
        post.setAllowComments(request.isAllowComments());
        Post updatedPost = postRepository.save(post);
        return PostResponse.builder()
                .id(updatedPost.getId())
                .caption(updatedPost.getCaption())
                .createdAt(updatedPost.getCreatedAt())
                .visibility(updatedPost.getVisibility())
                .isArchived(updatedPost.isArchived())
                .location(updatedPost.getLocation())
                .isReel(updatedPost.isReel())
                .collaborators(updatedPost.getCollaborators().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toSet())
                )
                .hideLikesAndViewCounts(updatedPost.isHideLikesAndViewCounts())
                .allowComments(updatedPost.isAllowComments())
                .postMedia(updatedPost.getPostMedia())
                .build();
    }
}
