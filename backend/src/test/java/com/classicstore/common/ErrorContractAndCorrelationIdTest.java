package com.classicstore.common;

import com.classicstore.common.controller.TestErrorController;
import com.classicstore.common.exception.GlobalExceptionHandler;
import com.classicstore.common.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestErrorController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({GlobalExceptionHandler.class, CorrelationIdFilter.class})
class ErrorContractAndCorrelationIdTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void businessExceptionReturns422WithExactJsonShape() throws Exception {
        String testCorrelationId = "test-correlation-id-12345";

        mockMvc.perform(get("/api/v1/test/business-error")
                        .header("X-Correlation-Id", testCorrelationId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("X-Correlation-Id", testCorrelationId))
                .andExpect(jsonPath("$.code", is("BUSINESS_RULE_VIOLATION")))
                .andExpect(jsonPath("$.messageAr", is("خطأ في تنفيذ العملية")))
                .andExpect(jsonPath("$.messageEn", is("Business rule violation occurred")))
                .andExpect(jsonPath("$.traceId", is(testCorrelationId)))
                .andExpect(jsonPath("$.details.reason", is("invalid_state")));
    }

    @Test
    @WithMockUser
    void correlationIdIsEchoedInResponseHeader() throws Exception {
        String customCorrelationId = "custom-uuid-98765";

        mockMvc.perform(get("/api/v1/test/ok")
                        .header("X-Correlation-Id", customCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", customCorrelationId));
    }
}
