package com.streamify.user;

import com.streamify.common.Mapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;

    public UserService(UserRepository userRepository, Mapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public User findUserById(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("This user is not found with ID: " + userId));
    }

    @Transactional
    public String followUser(String followingId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        User followUser = findUserById(followingId);

        // Add user to followUser's followers list
        followUser.getFollowers().add(user);
        followUser.setFollowerCount(followUser.getFollowerCount() + 1);

        // Add followUser to user's following list
        user.getFollowing().add(followUser);
        user.setFollowingCount(user.getFollowingCount() + 1);

        // Save both users to update both sides of the relationship
        userRepository.save(user);
        userRepository.save(followUser);

        return "Followed " + followUser.getUsername();
    }

    public List<UserDto> findUserFollowers(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return user.getFollowers()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public List<UserDto> findUserFollowings(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return user.getFollowing()
                .stream()
                .map(mapper::toUserDto)
                .toList();
    }
}
