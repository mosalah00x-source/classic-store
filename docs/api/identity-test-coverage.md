# Identity Module Test Coverage (R01–R19)

| Req | Scenario | Test class | Test method | Status |
|---|---|---|---|---|
| R01 | Register ignores "role":"ADMIN" in the request and creates a CUSTOMER | IdentityIntegrationTests | test2_registerWithRoleAdminCreatesCustomer | PASS |
| R02 | UserResponse JSON contains no passwordHash or password-related field | IdentityIntegrationTests | test4_serializedUserResponseDoesNotContainPasswordHash | PASS |
| R03 | Login with unknown email and login with wrong password return identical status and body | IdentityIntegrationTests | test5_loginUnknownEmailAndWrongPasswordReturnIdenticalStatusAndBody | PASS |
| R04 | Login with unknown email still calls PasswordEncoder.matches on the dummy hash | AuthServiceTimingUnitTest | loginWithUnknownUser_executesPasswordEncoderMatchesAgainstDummyHash | PASS |
| R05 | Refresh with a valid refresh token returns 200 and a new access token (typ=access) | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R06 | Refresh with an access token returns 401 INVALID_TOKEN | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R07 | Refresh with a tampered token returns 401 INVALID_TOKEN | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R08 | Refresh with an expired refresh token returns 401 INVALID_TOKEN | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R09 | The same refresh token is accepted again (no rotation) | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R10 | Logout returns 204 | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | PASS |
| R11 | A refresh token sent as Bearer returns 401 INVALID_TOKEN in the standard error shape | IdentityIntegrationTests | test14_to_16_bearerAuthenticationRules | PASS |
| R12 | An expired access token returns 401 INVALID_TOKEN | IdentityIntegrationTests | test14_to_16_bearerAuthenticationRules | PASS |
| R13 | Duplicate email returns 409 EMAIL_ALREADY_REGISTERED; duplicate phone returns 409 PHONE_ALREADY_REGISTERED | IdentityIntegrationTests | test3_duplicateEmailAndPhoneReturn409 | PASS |
| R14 | A customer lists only their own addresses | IdentityIntegrationTests | test17_to_20_addressesScenarios | PASS |
| R15 | GET /me/addresses/{id} for an address owned by another user returns 404, not 403 | IdentityIntegrationTests | test17_to_20_addressesScenarios | PASS |
| R16 | Two consecutive default-address changes succeed without a unique violation, and exactly one default remains | IdentityIntegrationTests | test17_to_20_addressesScenarios | PASS |
| R17 | A CUSTOMER calling an ADMIN-only endpoint returns 403 in the standard error shape | IdentityIntegrationTests | test21_customerOnAdminEndpoint_returns403ForbiddenWithErrorContract | ADDED |
| R18 | The application fails to start when JWT_SECRET is missing in the default profile | IdentityIntegrationTests | test23_missingJwtSecretProperty_causesApplicationContextFailure | ADDED |
| R19 | The test profile uses spring.jpa.hibernate.ddl-auto=validate | IdentityIntegrationTests | test22_testProfile_usesValidateDdlAuto | ADDED |
