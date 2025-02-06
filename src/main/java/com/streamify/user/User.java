package com.streamify.user;

import com.streamify.post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.security.auth.Subject;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // authentication fields
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phone;
    private String password;
    private boolean isVerified;

    // auditing
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDate createAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDate lastModifiedAt;

    // profile information
    @Column(unique = true)
    private String username;
    private String fullName;
    @Column(length = 150)
    private String bio;
    private String profilePictureUrl;
    private String website;
    private String gender;

    // setting & privacy
    private boolean isPrivate;
    private boolean allowMessageRequest;
    private boolean twoFactorEnable;

    // engagement & stats
    private int followerCount;
    private int followingCount;
    private int postsCount;

    // social & additional features
    private AccountStatus accountStatus;
    private LocalDateTime  lastLoginAt;

    // optional enhancements
    private LocalDateTime dateOfBirth;
    private String location;
    private String languagePreference;

    // relations
    @ManyToMany
    @JoinTable(
            name = "user_saved_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> savedPost = new LinkedHashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Token> tokens;

    @Override
    public String getName() {
        return fullName;
    }

    @Override
    public boolean implies(Subject subject) {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
