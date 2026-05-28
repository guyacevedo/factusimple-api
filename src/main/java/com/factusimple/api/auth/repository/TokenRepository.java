package com.factusimple.api.auth.repository;

import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Token> findByToken(String token);

    Optional<Token> findAllByUserAndRevokedAndTokenType(
            User user,
            boolean revoked,
            Token.TokenType tokenType
    );

    @EntityGraph(attributePaths = {"user"})
    @Query("""
        SELECT t FROM Token t
        WHERE t.user = :user
        AND t.tokenType = com.factusimple.api.auth.entity.Token$TokenType.REFRESH
        AND t.revoked = false
        ORDER BY t.createdAt DESC
    """)
    List<Token> findRefreshTokensByUserOrdered(User user);

    default Optional<Token> findLatestRefreshTokenByUser(User user) {
        return findRefreshTokensByUserOrdered(user).stream().findFirst();
    }

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Token t
        SET t.revoked = true
        WHERE t.user = :user
        AND t.revoked = false
    """)
    void revokeAllByUser(User user);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Token t
        SET t.revoked = true
        WHERE t.user = :user
        AND t.revoked = false
        AND t.tokenType IN (com.factusimple.api.auth.entity.Token$TokenType.FACTUS_ACCESS, com.factusimple.api.auth.entity.Token$TokenType.FACTUS_REFRESH)
    """)
    void revokeFactusTokensByUser(@Param("user") User user);

    @Query("""
        SELECT t FROM Token t
        WHERE t.user = :user
        AND t.revoked = false
        AND t.tokenType = :tokenType
        AND t.expiresAt > :now
        ORDER BY t.createdAt DESC
    """)
    Optional<Token> findByUserAndRevokedFalseAndTokenTypeAndExpiresAtAfter(
        @Param("user") User user,
        @Param("tokenType") Token.TokenType tokenType,
        @Param("now") LocalDateTime now
    );

}