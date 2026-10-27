package com.classicstore.identity.repository;

import com.classicstore.identity.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Query(value = "SELECT * FROM store.users WHERE email OPERATOR(store.=) CAST(:email AS store.citext)", nativeQuery = true)
    Optional<UserEntity> findByEmailCi(@Param("email") String email);

    Optional<UserEntity> findByPhone(String phone);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM store.users WHERE email OPERATOR(store.=) CAST(:email AS store.citext))", nativeQuery = true)
    boolean existsByEmailCi(@Param("email") String email);

    boolean existsByPhone(String phone);
}
