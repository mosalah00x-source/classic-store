package com.classicstore.identity.repository;

import com.classicstore.identity.domain.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    List<AddressEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<AddressEntity> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    int unsetDefaultForUser(@Param("userId") UUID userId);
}
