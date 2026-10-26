package com.classicstore.identity;

import com.classicstore.identity.domain.AddressEntity;
import com.classicstore.identity.domain.UserEntity;
import com.classicstore.identity.domain.UserRole;
import com.classicstore.identity.repository.AddressRepository;
import com.classicstore.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class IdentityMappingBehaviorProofTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.schemas", () -> "store");
        registry.add("spring.flyway.default-schema", () -> "store");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("jwt.secret", () -> "super-secret-jwt-key-for-testing-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void test1_userRoundTripAndCaseInsensitiveEmailLookup() {
        UserEntity user = new UserEntity();
        user.setEmail("Ali@Example.com");
        user.setPhone("+201098765432");
        user.setPasswordHash("$2a$10$hashedpasswordstring");
        user.setFirstName("Ali");
        user.setLastName("Hassan");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        UserEntity saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        // 1. Read back by ID and compare all fields
        UserEntity retrieved = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getEmail()).isEqualTo("Ali@Example.com");
        assertThat(retrieved.getPhone()).isEqualTo("+201098765432");
        assertThat(retrieved.getFirstName()).isEqualTo("Ali");
        assertThat(retrieved.getLastName()).isEqualTo("Hassan");
        assertThat(retrieved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(retrieved.isActive()).isTrue();

        // 2. Look up by email case-insensitively using lowercase "ali@example.com"
        Optional<UserEntity> foundCaseInsensitive = userRepository.findByEmailIgnoreCase("ali@example.com");
        assertThat(foundCaseInsensitive).isPresent();
        assertThat(foundCaseInsensitive.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void test2_addressRoundTripAndCountryCodeNoPadding() {
        UserEntity user = new UserEntity();
        user.setEmail("AddressUser@Example.com");
        user.setPhone("+201012345678");
        user.setPasswordHash("$2a$10$hashedpasswordstring");
        user.setFirstName("Address");
        user.setLastName("Test");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        UserEntity savedUser = userRepository.save(user);

        AddressEntity address = new AddressEntity();
        address.setUserId(savedUser.getId());
        address.setRecipientName("Recipient Test");
        address.setPhone("+201012345678");
        address.setStreet("Street 10");
        address.setCountryCode("EG");

        AddressEntity savedAddress = addressRepository.save(address);
        assertThat(savedAddress.getId()).isNotNull();

        AddressEntity retrievedAddress = addressRepository.findById(savedAddress.getId()).orElseThrow();
        assertThat(retrievedAddress.getCountryCode()).isEqualTo("EG");
        assertThat(retrievedAddress.getCountryCode().length()).isEqualTo(2);
    }
}
