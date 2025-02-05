package com.streamify.post;

import com.streamify.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "media_tags")
@EntityListeners(AuditingEntityListener.class)
public class MediaTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "post_media_id", nullable = false)
    private PostMedia postMedia;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User taggeduser;

    @Column(nullable = false)
    private Double xPosition;

    @Column(nullable = false)
    private Double yPosition;
}
