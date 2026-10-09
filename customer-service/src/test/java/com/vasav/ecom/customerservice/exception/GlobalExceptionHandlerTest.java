package com.vasav.ecom.customerservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    @RequestMapping("/t")
    static class ThrowingController {
        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("secret internal detail");
        }

        @GetMapping("/missing")
        String missing() {
            throw new ResourceNotFoundException("nope");
        }

        @GetMapping("/uuid/{id}")
        String uuid(@PathVariable UUID id) {
            return id.toString();
        }

        @GetMapping("/param")
        String param(@RequestParam boolean active) {
            return "ok";
        }

        @PostMapping("/body")
        String body(@RequestBody Map<String, String> body) {
            return "ok";
        }
    }

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        mvc.perform(get("/t/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void notFoundIs404() throws Exception {
        mvc.perform(get("/t/missing")).andExpect(status().isNotFound());
    }

    @Test
    void badUuidIs400() throws Exception {
        mvc.perform(get("/t/uuid/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void missingParamIs400() throws Exception {
        mvc.perform(get("/t/param")).andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/t/body").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongMethodIs405() throws Exception {
        mvc.perform(post("/t/missing")).andExpect(status().isMethodNotAllowed());
    }
}
