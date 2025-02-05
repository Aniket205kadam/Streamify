package com.streamify.security;

import com.streamify.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the email: " + identifier +". Please check again or register!")
                    );
        } else if (identifier.matches("^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,4}[- ]?\\d{1,4}$")) {
            return userRepository.findByPhone(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the phone: " + identifier +". Please check again or register!")
                    );
        } else {
            return userRepository.findByUsername(identifier)
                    .orElseThrow(() ->
                            new EntityNotFoundException("We couldn't find a user with the username: " + identifier +". Please check again or register!")
                    );
        }
    }
}
