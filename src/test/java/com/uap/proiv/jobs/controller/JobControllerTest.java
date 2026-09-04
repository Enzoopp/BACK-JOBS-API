package com.uap.proiv.jobs.controller;

import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Unitario: JobService va mockeado, solo se prueba la capa HTTP.
@ExtendWith(MockitoExtension.class)
public class JobControllerTest {

    @Mock
    JobService jobService;

    @InjectMocks
    JobController jobController;

    private MockMvc mockMvc;

    private List<Job> jobs;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();

        jobs = new ArrayList<>();

        Job job1 = new Job();
        job1.setId(1);
        job1.setName("Data Engineer");
        job1.setSalary(5000);
        job1.setHours(530);
        job1.setResources(3);
        jobs.add(job1);

        Job job2 = new Job();
        job2.setId(2);
        job2.setName("Frontend Engineer");
        job2.setSalary(6000);
        job2.setHours(450);
        job2.setResources(3);
        jobs.add(job2);
    }

    @Test
    @DisplayName("GET /api/job/all - Caso de exito: retorna el listado completo de trabajos")
    void getAllJobs_success() throws Exception {
        when(jobService.getAllJobs()).thenReturn(jobs);

        mockMvc.perform(get("/api/job/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Data Engineer"))
                .andExpect(jsonPath("$[0].resources").value(3))
                .andExpect(jsonPath("$[1].name").value("Frontend Engineer"));
    }

    @Test
    @DisplayName("GET /api/job/all - Camino de excepcion: el service falla y el controller responde 500 con el mensaje")
    void getAllJobs_exception() throws Exception {
        when(jobService.getAllJobs()).thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/job/all"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Service Error"));
    }

    @Test
    @DisplayName("GET /api/job/{id} - Caso de exito: retorna el trabajo solicitado")
    void getJobById_success() throws Exception {
        when(jobService.getJobById(1)).thenReturn(jobs.get(0));

        mockMvc.perform(get("/api/job/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Data Engineer"))
                .andExpect(jsonPath("$.salary").value(5000))
                .andExpect(jsonPath("$.hours").value(530));
    }

    @Test
    @DisplayName("GET /api/job/{id} - Camino de excepcion: id inexistente responde 500 con el mensaje del service")
    void getJobById_exception() throws Exception {
        when(jobService.getJobById(999)).thenThrow(new NoSuchElementException("No value present"));

        mockMvc.perform(get("/api/job/999"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("No value present"));
    }
}
