package com.classicstore.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtService(JwtProperties jwtProperties, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.objectMapper = objectMapper;
    }

    public String generateAccessToken(UUID userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(userId, claims, TYPE_ACCESS, jwtProperties.getAccessTokenExpirationSeconds());
    }

    public String generateRefreshToken(UUID userId) {
        return createToken(userId, Map.of(), TYPE_REFRESH, jwtProperties.getRefreshTokenExpirationSeconds());
    }

    private String createToken(UUID userId, Map<String, Object> additionalClaims, String type, long ttlSeconds) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + ttlSeconds;

            Map<String, Object> header = Map.of("alg", "HS256", "typ", type);
            Map<String, Object> payload = new HashMap<>(additionalClaims);
            payload.put("sub", userId.toString());
            payload.put("typ", type);
            payload.put("iat", now);
            payload.put("exp", exp);

            String headerB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));

            String contentToSign = headerB64 + "." + payloadB64;
            String signatureB64 = sign(contentToSign);

            return contentToSign + "." + signatureB64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    @SuppressWarnings("unchecked")
    public TokenClaims validateAndParseToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenClaims.invalid();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return TokenClaims.invalid();
        }

        String contentToSign = parts[0] + "." + parts[1];
        String expectedSig = sign(contentToSign);

        if (!MessageDigest.isEqual(expectedSig.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return TokenClaims.invalid();
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, Map.class);

            String sub = (String) payload.get("sub");
            String role = (String) payload.get("role");
            String typ = (String) payload.get("typ");

            if (sub == null) {
                return TokenClaims.invalid();
            }

            Number expNum = (Number) payload.get("exp");
            boolean expired = false;
            if (expNum != null) {
                long exp = expNum.longValue();
                if (Instant.now().getEpochSecond() > exp) {
                    expired = true;
                }
            }

            return new TokenClaims(UUID.fromString(sub), role, typ, true, expired);
        } catch (Exception e) {
            return TokenClaims.invalid();
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    public static record TokenClaims(UUID userId, String role, String tokenType, boolean validSignature, boolean expired) {
        public static TokenClaims invalid() {
            return new TokenClaims(null, null, null, false, false);
        }

        public boolean isValid(String expectedType) {
            return validSignature && !expired && expectedType.equals(tokenType);
        }
    }
}
