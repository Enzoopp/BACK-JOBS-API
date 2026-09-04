package com.uap.proiv.jobs.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracion de JobController. No hace falta mockear nada: los jobs salen de jobs.json (local).
@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/job/all - devuelve los 7 trabajos de jobs.json")
    void getAllJobs_integracion() throws Exception {
        mockMvc.perform(get("/api/job/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].name").value("Data Engineer"))
                .andExpect(jsonPath("$[0].resources").value(3))
                .andExpect(jsonPath("$[6].name").value("Full Stack Developer"))
                .andExpect(jsonPath("$[6].resources").value(10));
    }

    @Test
    @DisplayName("GET /api/job/{id} - devuelve el trabajo correspondiente")
    void getJobById_integracion_success() throws Exception {
        mockMvc.perform(get("/api/job/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("QA Engineer"))
                .andExpect(jsonPath("$.salary").value(3600))
                .andExpect(jsonPath("$.hours").value(360))
                .andExpect(jsonPath("$.resources").value(2));
    }

    @Test
    @DisplayName("GET /api/job/{id} - id inexistente responde 500")
    void getJobById_integracion_idInexistente() throws Exception {
        // JobServiceImpl.getJobById() usa orElseThrow() sin mensaje, y el controller lo atrapa devolviendo 500.
        mockMvc.perform(get("/api/job/9999"))
                .andExpect(status().is5xxServerError());
    }
}
