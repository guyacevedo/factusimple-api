package com.factusimple.api.auth.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import com.factusimple.api.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
        @Index(name = "idx_token_user_id", columnList = "user_id"),
        @Index(name = "idx_token_expires_at", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Token extends BaseEntity {

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "El token no puede estar vacío")
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(columnDefinition = "TEXT")
    private String factusToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}