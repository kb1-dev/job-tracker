package com.kbl_dev.job_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApplicationApiIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createThenUpdateStatus_persistsHistoryEndToEnd() throws Exception {
        String createBody = """
                {
                  "company": "Acme Corp",
                  "title": "Backend Engineer"
                }
                """;

        String response = mockMvc.perform(post("/applications")
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        String statusUpdateBody = """
                {
                  "status": "PHONE_SCREEN",
                  "note": "Recruiter reached out"
                }
                """;

        mockMvc.perform(patch("/applications/{id}/status", id)
                        .contentType("application/json")
                        .content(statusUpdateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PHONE_SCREEN"));

        mockMvc.perform(get("/applications/{id}/status-history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PHONE_SCREEN"))
                .andExpect(jsonPath("$[0].note").value("Recruiter reached out"));
    }

    @Test
    void updateStatus_returns400OnNoOpTransition() throws Exception {
        String createBody = """
                { "company": "Acme Corp", "title": "Backend Engineer" }
                """;

        String response = mockMvc.perform(post("/applications")
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        String sameStatusBody = """
                { "status": "APPLIED" }
                """;

        mockMvc.perform(patch("/applications/{id}/status", id)
                        .contentType("application/json")
                        .content(sameStatusBody))
                .andExpect(status().is4xxClientError());
    }
}