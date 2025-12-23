package com.isysnap.repository;

import com.isysnap.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
        SELECT t FROM Token t
        WHERE t.user.email = :email
        AND t.expired = false
        AND t.revoked = false
    """)
    List<Token> findAllValidTokenByUser(String email);

    Optional<Token> findByToken(String token);

    @Modifying
    @Query("""
        UPDATE Token t
        SET t.expired = true, t.revoked = true
        WHERE t.user.id = :userId
    """)
    void invalidateAllUserTokens(String userId);
}
