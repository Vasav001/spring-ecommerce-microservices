package com.vasav.ecom.catalogservice;

import com.vasav.ecom.catalogservice.config.CorrelationIdFilter;
import com.vasav.ecom.catalogservice.config.TestJwts;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityTests extends AbstractIntegrationTest {

    @Test
    void adminApiWithoutTokenIs401() throws Exception {
        mvc.perform(get(PRODUCTS)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminApiAsCustomerIs403() throws Exception {
        mvc.perform(get(PRODUCTS).with(TestJwts.customer("c-1"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/reviews/" + UUID.randomUUID() + "/hide").with(TestJwts.customer("c-1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenWithoutRealmRolesIs403OnAdminApi() throws Exception {
        mvc.perform(get(PRODUCTS).with(TestJwts.user("no-roles", "No", "Roles"))).andExpect(status().isForbidden());
    }

    @Test
    void adminApiAsAdminIs200() throws Exception {
        mvc.perform(get(PRODUCTS).with(TestJwts.admin())).andExpect(status().isOk());
    }

    @Test
    void publicReviewListWithoutTokenIs200() throws Exception {
        mvc.perform(get("/api/v1/reviews").param("productId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void otherReviewEndpointsNeedToken() throws Exception {
        mvc.perform(get("/api/v1/reviews/eligibility").param("productId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/reviews")).andExpect(status().isUnauthorized());
    }

    @Test
    void correlationIdIsEchoedOrGenerated() throws Exception {
        mvc.perform(get(PRODUCTS).with(TestJwts.admin()).header(CorrelationIdFilter.HEADER, "req-123"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, "req-123"));
        // also on responses produced by the security filter chain
        mvc.perform(get(PRODUCTS))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern("[0-9a-f-]{36}")));
        // unsafe values are replaced, not logged
        mvc.perform(get(PRODUCTS).with(TestJwts.admin()).header(CorrelationIdFilter.HEADER, "bad value\nx"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern("[0-9a-f-]{36}")));
    }
}
