package com.uap.proiv.jobs.controller;

import com.uap.proiv.jobs.controller.AssignController;
import com.uap.proiv.jobs.controller.UserController;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserJobAssignedService;
import com.uap.proiv.jobs.service.UserService;

import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.dto.AssignRequest;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)

public class JobControllerTest{

    @Mock
    UserService userService;

    @Mock
    JobService jobService;

    @Mock
    UserJobAssignedService userJobAssignedService;

    @InjectMocks
    JobController jobController;

    @InjectMocks
    UserController userController;

    @InjectMocks
    AssignController assignController;

    private MockMvc mockMvc;    

    private UserApiResponse userApiResponse;
    private List<User> users;
    private ObjectMapper objectMapper;
    

    @BeforeEach
    void setup() {
        
        mockMvc = MockMvcBuilders.standaloneSetup(jobController, userController, assignController).build();
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




        userApiResponse = new UserApiResponse();
        userApiResponse.setPage(1);
        userApiResponse.setPerPage(2);
        userApiResponse.setTotal(2);
        userApiResponse.setTotalPages(1);
        userApiResponse.setData(users);

    }

        @Test 
        @DisplayName("GET api /api/job/users/{page} retorna usuarios")

        void getUsers_success_initial_data() throws Exception {

        when(userService.search(1))
        .thenReturn(userApiResponse)              
        .thenThrow(new RuntimeException("MSG"))  
        .thenReturn(userApiResponse);

        mockMvc.perform(get("/api/user/1")).andExpect((status().isOk()))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath(("$.page")).value(1))
        .andExpect(jsonPath("$.total").value(2));

        mockMvc.perform(get("/api/user/1")).andExpect((status().is5xxServerError()));

        mockMvc.perform(get("/api/user/1")).andExpect((status().isOk()));
    }  
    @Test
    @DisplayName("GET api /api/job/users/{page} - Excepcion retornada por el service")
    void getUsers_exception() throws Exception {
        when(userService.search(2)).thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/user/2")).andExpect(status().is5xxServerError())
        .andExpect(content().string("Service Error"));
    }

            @Test 
        @DisplayName("GET api /api/job/users/{page} retorna usuarios")

        void getUsers_success_set_page() throws Exception {

        userApiResponse.setPage(3);
        when(userService.search(1)).thenReturn(userApiResponse);

        mockMvc.perform(get("/api/user/1")).andExpect((status().isOk()))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath(("$.page")).value(3))
        .andExpect(jsonPath("$.total").value(2));
    }  

    @Test
    @DisplayName("POST /api/job/assign - Asignar trabajo a usuario")
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
