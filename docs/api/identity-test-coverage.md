# Identity Module Test Coverage (R01–R19)

| Req | السيناريو | Test class | Test method | الحالة |
|---|---|---|---|---|
| R01 | Register ينشئ CUSTOMER حتى لو الـ payload فيه role: ADMIN | IdentityIntegrationTests | test2_registerWithRoleAdminCreatesCustomer | ✅ |
| R02 | UserResponse مفيهوش passwordHash | IdentityIntegrationTests | test4_serializedUserResponseDoesNotContainPasswordHash | ✅ |
| R03 | Login: unknown user و wrong password نفس الـ body | IdentityIntegrationTests | test5_loginUnknownEmailAndWrongPasswordReturnIdenticalStatusAndBody | ✅ |
| R04 | Timing: matches() بيتنادى على المسار غير الموجود | AuthServiceTimingUnitTest | loginWithUnknownUser_executesPasswordEncoderMatchesAgainstDummyHash | ✅ |
| R05 | Refresh بيرجع access token جديد | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R06 | Refresh بـ access token يرجع 401 INVALID_TOKEN | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R07 | Refresh tampered يرجع 401 | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R08 | Refresh منتهي يرجع 401 | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R09 | Refresh token بيتقبل مرة تانية (مفيش rotation) | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R10 | Logout يرجع 204 | IdentityIntegrationTests | test7_to_13_refreshAndLogoutBehavior | ✅ |
| R11 | Refresh token كـ Bearer يرجع 401 INVALID_TOKEN | IdentityIntegrationTests | test14_to_16_bearerAuthenticationRules | ✅ |
| R12 | Access token منتهي يرجع 401 | IdentityIntegrationTests | test14_to_16_bearerAuthenticationRules | ✅ |
| R13 | Duplicate email 409، duplicate phone 409 | IdentityIntegrationTests | test3_duplicateEmailAndPhoneReturn409 | ✅ |
| R14 | Customer يشوف عناوينه بس | IdentityIntegrationTests | test17_to_20_addressesScenarios | ✅ |
| R15 | عنوان غير مملوك يرجع 404 مش 403 | IdentityIntegrationTests | test17_to_20_addressesScenarios | ✅ |
| R16 | تغييرين متتاليين للعنوان الافتراضي من غير unique violation | IdentityIntegrationTests | test17_to_20_addressesScenarios | ✅ |
| R17 | CUSTOMER على endpoint ADMIN يرجع 403 بالـ error contract | IdentityIntegrationTests | test21_customerOnAdminEndpoint_returns403ForbiddenWithErrorContract | ✅ |
| R18 | التطبيق مش بيقوم من غير JWT_SECRET | IdentityIntegrationTests | test23_missingJwtSecretProperty_causesApplicationContextFailure | ✅ |
| R19 | ddl-auto=validate في الـ test profile | IdentityIntegrationTests | test22_testProfile_usesValidateDdlAuto | ✅ |
