package com.kbl_dev.job_tracker;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InterviewApiIT extends IntegrationTestBase {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createThenListInterviews_persistsEndToEnd() throws Exception {
        String createAppBody = """
                { "company": "Acme Corp", "title": "Backend Engineer" }
                """;

        String appResponse = mockMvc.perform(post("/applications")
                        .contentType("application/json")
                        .content(createAppBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String applicationId = objectMapper.readTree(appResponse).get("id").asText();

        String createInterviewBody = """
                { "round": "Phone Screen", "interviewerName": "Jane Doe" }
                """;

        mockMvc.perform(post("/applications/{id}/interviews", applicationId)
                        .contentType("application/json")
                        .content(createInterviewBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("PENDING"));

        mockMvc.perform(get("/applications/{id}/interviews", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].round").value("Phone Screen"));
    }

    @Test
    void createInterview_returns404WhenApplicationDoesNotExist() throws Exception {
        String createInterviewBody = """
                { "round": "Phone Screen" }
                """;

        mockMvc.perform(post("/applications/{id}/interviews", java.util.UUID.randomUUID())
                        .contentType("application/json")
                        .content(createInterviewBody))
                .andExpect(status().isNotFound());
    }
}