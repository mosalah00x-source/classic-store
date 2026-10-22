package com.classicstore.identity;

import com.classicstore.common.exception.ApiErrorResponse;
import com.classicstore.identity.domain.UserEntity;
import com.classicstore.identity.domain.UserRole;
import com.classicstore.identity.dto.AddressInput;
import com.classicstore.identity.dto.AddressResponse;
import com.classicstore.identity.dto.AuthResponse;
import com.classicstore.identity.dto.LoginRequest;
import com.classicstore.identity.dto.RefreshTokenRequest;
import com.classicstore.identity.dto.RefreshResponse;
import com.classicstore.identity.dto.RegisterRequest;
import com.classicstore.identity.dto.UserResponse;
import com.classicstore.identity.repository.UserRepository;
import com.classicstore.identity.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class IdentityIntegrationTests {

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
        registry.add("jwt.secret", () -> "super-secret-jwt-key-for-testing-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test1_registerThenLoginSucceeds_andPasswordIsBCryptHash() {
        RegisterRequest registerReq = new RegisterRequest("Test User 1", "user1@example.com", "+201011111111", "Password123!");
        ResponseEntity<UserResponse> regResp = restTemplate.postForEntity("/api/v1/auth/register", registerReq, UserResponse.class);

        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(regResp.getBody()).isNotNull();
        assertThat(regResp.getBody().getId()).isNotNull();

        Optional<UserEntity> userOpt = userRepository.findById(regResp.getBody().getId());
        assertThat(userOpt).isPresent();
        String passwordHash = userOpt.get().getPasswordHash();
        assertThat(passwordHash).startsWith("$2a$");

        LoginRequest loginReq = new LoginRequest("user1@example.com", "Password123!");
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity("/api/v1/auth/login", loginReq, AuthResponse.class);

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        assertThat(loginResp.getBody().getAccessToken()).isNotBlank();
        assertThat(loginResp.getBody().getRefreshToken()).isNotBlank();
        assertThat(loginResp.getBody().getExpiresIn()).isEqualTo(900);
    }

    @Test
    void test2_registerWithRoleAdminCreatesCustomer() {
        // Sending raw JSON containing "role": "ADMIN"
        Map<String, Object> reqBody = Map.of(
                "name", "Admin Attempt",
                "email", "adminattempt@example.com",
                "phone", "+201011112222",
                "password", "Password123!",
                "role", "ADMIN"
        );

        ResponseEntity<UserResponse> regResp = restTemplate.postForEntity("/api/v1/auth/register", reqBody, UserResponse.class);
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(regResp.getBody()).isNotNull();
        assertThat(regResp.getBody().getRole()).isEqualTo(UserRole.CUSTOMER);

        UserEntity entity = userRepository.findById(regResp.getBody().getId()).orElseThrow();
        assertThat(entity.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void test3_duplicateEmailAndPhoneReturn409() {
        RegisterRequest r1 = new RegisterRequest("User Dup", "dup@example.com", "+201099999999", "Password123!");
        restTemplate.postForEntity("/api/v1/auth/register", r1, UserResponse.class);

        // Duplicate email
        RegisterRequest rDupEmail = new RegisterRequest("User Dup E", "dup@example.com", "+201088888888", "Password123!");
        ResponseEntity<ApiErrorResponse> respDupE = restTemplate.postForEntity("/api/v1/auth/register", rDupEmail, ApiErrorResponse.class);
        assertThat(respDupE.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respDupE.getBody().code()).isEqualTo("EMAIL_ALREADY_REGISTERED");

        // Duplicate phone
        RegisterRequest rDupPhone = new RegisterRequest("User Dup P", "otherdup@example.com", "+201099999999", "Password123!");
        ResponseEntity<ApiErrorResponse> respDupP = restTemplate.postForEntity("/api/v1/auth/register", rDupPhone, ApiErrorResponse.class);
        assertThat(respDupP.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respDupP.getBody().code()).isEqualTo("PHONE_ALREADY_REGISTERED");
    }

    @Test
    void test4_serializedUserResponseDoesNotContainPasswordHash() throws Exception {
        RegisterRequest r1 = new RegisterRequest("User Serialized", "serial@example.com", "+201077777777", "Password123!");
        ResponseEntity<UserResponse> regResp = restTemplate.postForEntity("/api/v1/auth/register", r1, UserResponse.class);

        String json = objectMapper.writeValueAsString(regResp.getBody());
        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("passwordHash");
    }

    @Test
    void test5_loginUnknownEmailAndWrongPasswordReturnIdenticalStatusAndBody() {
        RegisterRequest r1 = new RegisterRequest("User LoginCheck", "logincheck@example.com", "+201066666666", "CorrectPass123!");
        restTemplate.postForEntity("/api/v1/auth/register", r1, UserResponse.class);

        // Unknown email
        LoginRequest unknownReq = new LoginRequest("unknownlogin@example.com", "WrongPass123!");
        ResponseEntity<ApiErrorResponse> respUnknown = restTemplate.postForEntity("/api/v1/auth/login", unknownReq, ApiErrorResponse.class);

        // Wrong password
        LoginRequest wrongPassReq = new LoginRequest("logincheck@example.com", "WrongPass123!");
        ResponseEntity<ApiErrorResponse> respWrongPass = restTemplate.postForEntity("/api/v1/auth/login", wrongPassReq, ApiErrorResponse.class);

        assertThat(respUnknown.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(respWrongPass.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(respUnknown.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(respWrongPass.getBody().code()).isEqualTo("INVALID_CREDENTIALS");

        assertThat(respUnknown.getBody().messageAr()).isEqualTo(respWrongPass.getBody().messageAr());
        assertThat(respUnknown.getBody().messageEn()).isEqualTo(respWrongPass.getBody().messageEn());
    }

    @Test
    void test7_to_13_refreshAndLogoutBehavior() {
        RegisterRequest r1 = new RegisterRequest("User Refresh", "refresh@example.com", "+201055554444", "Password123!");
        restTemplate.postForEntity("/api/v1/auth/register", r1, UserResponse.class);
        AuthResponse loginAuth = restTemplate.postForEntity("/api/v1/auth/login", new LoginRequest("refresh@example.com", "Password123!"), AuthResponse.class).getBody();

        String accessToken = loginAuth.getAccessToken();
        String refreshToken = loginAuth.getRefreshToken();

        // 7. Refresh with valid refresh token returns 200 and new access token
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(refreshToken);
        ResponseEntity<RefreshResponse> refreshResp = restTemplate.postForEntity("/api/v1/auth/refresh", refreshReq, RefreshResponse.class);
        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResp.getBody().getAccessToken()).isNotBlank();
        assertThat(refreshResp.getBody().getExpiresIn()).isEqualTo(900);

        // Verify newly issued token has typ: access
        JwtService.TokenClaims newClaims = jwtService.validateAndParseToken(refreshResp.getBody().getAccessToken());
        assertThat(newClaims.isValid(JwtService.TYPE_ACCESS)).isTrue();

        // 8. Refresh with access token returns 401 INVALID_TOKEN
        RefreshTokenRequest invalidTypeReq = new RefreshTokenRequest(accessToken);
        ResponseEntity<ApiErrorResponse> invalidTypeResp = restTemplate.postForEntity("/api/v1/auth/refresh", invalidTypeReq, ApiErrorResponse.class);
        assertThat(invalidTypeResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(invalidTypeResp.getBody().code()).isEqualTo("INVALID_TOKEN");

        // 9. Refresh with tampered token returns 401 INVALID_TOKEN
        RefreshTokenRequest tamperedReq = new RefreshTokenRequest(refreshToken + "tampered");
        ResponseEntity<ApiErrorResponse> tamperedResp = restTemplate.postForEntity("/api/v1/auth/refresh", tamperedReq, ApiErrorResponse.class);
        assertThat(tamperedResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(tamperedResp.getBody().code()).isEqualTo("INVALID_TOKEN");

        // 10. Refresh with expired refresh token returns 401 INVALID_TOKEN
        UserEntity user = userRepository.findByEmailIgnoreCase("refresh@example.com").orElseThrow();
        String expiredRefreshToken = createExpiredToken(user.getId(), JwtService.TYPE_REFRESH);
        RefreshTokenRequest expiredReq = new RefreshTokenRequest(expiredRefreshToken);
        ResponseEntity<ApiErrorResponse> expiredResp = restTemplate.postForEntity("/api/v1/auth/refresh", expiredReq, ApiErrorResponse.class);
        assertThat(expiredResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(expiredResp.getBody().code()).isEqualTo("INVALID_TOKEN");

        // 11. Refresh token accepted again on second refresh call (stateless, no rotation)
        ResponseEntity<RefreshResponse> refreshResp2 = restTemplate.postForEntity("/api/v1/auth/refresh", refreshReq, RefreshResponse.class);
        assertThat(refreshResp2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 12. Logout returns 204
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Void> logoutResp = restTemplate.exchange("/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 13. After logout, same refresh token still works on /auth/refresh
        ResponseEntity<RefreshResponse> refreshRespAfterLogout = restTemplate.postForEntity("/api/v1/auth/refresh", refreshReq, RefreshResponse.class);
        assertThat(refreshRespAfterLogout.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test14_to_16_bearerAuthenticationRules() {
        RegisterRequest r1 = new RegisterRequest("User Bearer", "bearer@example.com", "+201033332222", "Password123!");
        restTemplate.postForEntity("/api/v1/auth/register", r1, UserResponse.class);
        AuthResponse loginAuth = restTemplate.postForEntity("/api/v1/auth/login", new LoginRequest("bearer@example.com", "Password123!"), AuthResponse.class).getBody();

        UserEntity user = userRepository.findByEmailIgnoreCase("bearer@example.com").orElseThrow();

        // 14. Refresh token sent as Authorization: Bearer returns 401 INVALID_TOKEN
        HttpHeaders refreshBearerHeaders = new HttpHeaders();
        refreshBearerHeaders.setBearerAuth(loginAuth.getRefreshToken());
        ResponseEntity<ApiErrorResponse> refreshBearerResp = restTemplate.exchange(
                "/api/v1/me/addresses", HttpMethod.GET, new HttpEntity<>(refreshBearerHeaders), ApiErrorResponse.class);
        assertThat(refreshBearerResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(refreshBearerResp.getBody().code()).isEqualTo("INVALID_TOKEN");

        // 15. Expired access token returns 401 INVALID_TOKEN
        String expiredAccessToken = createExpiredToken(user.getId(), JwtService.TYPE_ACCESS);
        HttpHeaders expiredBearerHeaders = new HttpHeaders();
        expiredBearerHeaders.setBearerAuth(expiredAccessToken);
        ResponseEntity<ApiErrorResponse> expiredBearerResp = restTemplate.exchange(
                "/api/v1/me/addresses", HttpMethod.GET, new HttpEntity<>(expiredBearerHeaders), ApiErrorResponse.class);
        assertThat(expiredBearerResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(expiredBearerResp.getBody().code()).isEqualTo("INVALID_TOKEN");

        // 16. Access token with typ=access works on GET /me/addresses
        HttpHeaders validBearerHeaders = new HttpHeaders();
        validBearerHeaders.setBearerAuth(loginAuth.getAccessToken());
        ResponseEntity<List<AddressResponse>> validBearerResp = restTemplate.exchange(
                "/api/v1/me/addresses", HttpMethod.GET, new HttpEntity<>(validBearerHeaders), new ParameterizedTypeReference<List<AddressResponse>>() {});
        assertThat(validBearerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test17_to_20_addressesScenarios() {
        // Register Customer 1
        RegisterRequest c1 = new RegisterRequest("Cust One", "cust1addr@example.com", "+201011113333", "Password123!");
        restTemplate.postForEntity("/api/v1/auth/register", c1, UserResponse.class);
        AuthResponse c1Auth = restTemplate.postForEntity("/api/v1/auth/login", new LoginRequest("cust1addr@example.com", "Password123!"), AuthResponse.class).getBody();

        // Register Customer 2
        RegisterRequest c2 = new RegisterRequest("Cust Two", "cust2addr@example.com", "+201011114444", "Password123!");
        restTemplate.postForEntity("/api/v1/auth/register", c2, UserResponse.class);
        AuthResponse c2Auth = restTemplate.postForEntity("/api/v1/auth/login", new LoginRequest("cust2addr@example.com", "Password123!"), AuthResponse.class).getBody();

        HttpHeaders c1Headers = new HttpHeaders();
        c1Headers.setBearerAuth(c1Auth.getAccessToken());
        c1Headers.setContentType(MediaType.APPLICATION_JSON);

        // 17. Customer 1 adds address
        AddressInput a1Input = new AddressInput();
        a1Input.setRecipientName("Cust 1 Recipient");
        a1Input.setPhone("+201011113333");
        a1Input.setLine1("Street 1");
        a1Input.setIsDefault(true);

        ResponseEntity<AddressResponse> addA1Resp = restTemplate.exchange("/api/v1/me/addresses", HttpMethod.POST, new HttpEntity<>(a1Input, c1Headers), AddressResponse.class);
        assertThat(addA1Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID a1Id = addA1Resp.getBody().getId();

        // 18. Customer 2 tries GET /me/addresses/{id} for Customer 1's address -> 404
        HttpHeaders c2Headers = new HttpHeaders();
        c2Headers.setBearerAuth(c2Auth.getAccessToken());
        ResponseEntity<ApiErrorResponse> getUnownedResp = restTemplate.exchange("/api/v1/me/addresses/" + a1Id, HttpMethod.GET, new HttpEntity<>(c2Headers), ApiErrorResponse.class);
        assertThat(getUnownedResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getUnownedResp.getBody().code()).isEqualTo("NOT_FOUND");

        // Customer 1 GET /me/addresses/{id} for own address -> 200
        ResponseEntity<AddressResponse> getOwnedResp = restTemplate.exchange("/api/v1/me/addresses/" + a1Id, HttpMethod.GET, new HttpEntity<>(c1Headers), AddressResponse.class);
        assertThat(getOwnedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getOwnedResp.getBody().getId()).isEqualTo(a1Id);

        // 19. Two consecutive default-address changes succeed without constraint violation
        AddressInput a2Input = new AddressInput();
        a2Input.setRecipientName("Cust 1 Recipient 2");
        a2Input.setPhone("+201011113333");
        a2Input.setLine1("Street 2");
        a2Input.setIsDefault(true);

        ResponseEntity<AddressResponse> addA2Resp = restTemplate.exchange("/api/v1/me/addresses", HttpMethod.POST, new HttpEntity<>(a2Input, c1Headers), AddressResponse.class);
        assertThat(addA2Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID a2Id = addA2Resp.getBody().getId();

        // 20. Exactly one default address remains
        ResponseEntity<List<AddressResponse>> listResp = restTemplate.exchange("/api/v1/me/addresses", HttpMethod.GET, new HttpEntity<>(c1Headers), new ParameterizedTypeReference<List<AddressResponse>>() {});
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AddressResponse> addresses = listResp.getBody();
        assertThat(addresses).hasSize(2);

        long defaultCount = addresses.stream().filter(AddressResponse::isDefault).count();
        assertThat(defaultCount).isEqualTo(1);

        AddressResponse defaultAddress = addresses.stream().filter(AddressResponse::isDefault).findFirst().orElseThrow();
        assertThat(defaultAddress.getId()).isEqualTo(a2Id);
    }

    private String createExpiredToken(UUID userId, String type) {
        try {
            long now = System.currentTimeMillis() / 1000 - 3600; // 1 hour in the past
            long exp = now - 100;

            Map<String, Object> header = Map.of("alg", "HS256", "typ", type);
            Map<String, Object> payload = Map.of(
                    "sub", userId.toString(),
                    "role", "CUSTOMER",
                    "typ", type,
                    "iat", now,
                    "exp", exp
            );

            String headerB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));

            String contentToSign = headerB64 + "." + payloadB64;

            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec("super-secret-jwt-key-for-testing-must-be-at-least-32-bytes-long".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(contentToSign.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String signatureB64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);

            return contentToSign + "." + signatureB64;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
