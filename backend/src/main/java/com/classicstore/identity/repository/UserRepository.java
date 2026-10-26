package com.classicstore.identity.repository;

import com.classicstore.identity.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Query(value = "select * from store.users where email = cast(:email as citext)", nativeQuery = true)
    Optional<UserEntity> findByEmail(@Param("email") String email);

    Optional<UserEntity> findByPhone(String phone);

    @Query(value = "select exists (select 1 from store.users where email = cast(:email as citext))", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    boolean existsByPhone(String phone);
}
