package com.classicstore.identity.repository;

import com.classicstore.identity.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Query(value = "SELECT * FROM store.users WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    Optional<UserEntity> findByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT * FROM store.users WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    Optional<UserEntity> findByEmail(@Param("email") String email);

    Optional<UserEntity> findByPhone(String phone);

    @Query(value = "SELECT COUNT(*) > 0 FROM store.users WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM store.users WHERE LOWER(email) = LOWER(:email)", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    boolean existsByPhone(String phone);
}
