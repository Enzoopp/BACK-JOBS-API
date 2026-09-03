package com.uap.proiv.jobs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.dto.AssignRequest;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.UserJobAssignedService;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)

public class AssignControllerTest {

    @Mock
    UserJobAssignedService userJobAssignedService;

    @InjectMocks
    AssignController assignController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private List<User> users;

    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(assignController).build();
        objectMapper = new ObjectMapper();

        users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("ejemplo@as.com");
        user1.setAvatar("null");
        user1.setFirstName("juan");
        user1.setLastName("Garcia");
        users.add(user1);

        User user2 = new User();
        user2.setId(2);
        user2.setEmail("ejemplo2@as.com");
        user2.setAvatar("null");
        user2.setFirstName("diane");
        user2.setLastName("perez");
        users.add(user2);
    }

    @Test
    @DisplayName("POST /api/assign - Asignar trabajo a usuario")
    void postAssign_success() throws Exception {
        AssignRequest assignRequest = new AssignRequest();
        assignRequest.setRequestNumber(123);
        assignRequest.setClientName("Name");

        Job job1 = new Job();
        job1.setId(1);
        job1.setName("Developer");
        job1.setSalary(5000);
        job1.setHours(2000);
        job1.setResources(3);

        Job job2 = new Job();
        job2.setId(2);
        job2.setName("Designer");
        job2.setSalary(4500);
        job2.setHours(1500);
        job2.setResources(1);

        List<UserJobAssigned> userJobAssignedList = new ArrayList<>();
        userJobAssignedList.add(new UserJobAssigned(users, job1));
        userJobAssignedList.add(new UserJobAssigned(List.of(users.getFirst()), job2));

        when(userJobAssignedService.assign()).thenReturn(userJobAssignedList);

        mockMvc.perform(post("/api/assign")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(assignRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.Assign").isNotEmpty())
        .andExpect(jsonPath("$.Assign[0].job.name").value("Developer"))
        .andExpect(jsonPath("$.Assign[1].job.name").value("Designer"))
        .andExpect(jsonPath("$.Assign[0].users[0].first_name").value("juan"))
        .andExpect(jsonPath("$.Request_Number").value(123))
        .andExpect(jsonPath("$.Client").value("Name"));
    }
}