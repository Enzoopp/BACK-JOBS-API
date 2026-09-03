package com.uap.proiv.jobs.controller;

import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.service.UserService;

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

public class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    private MockMvc mockMvc;

    private UserApiResponse userApiResponse;
    private List<User> users;

    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

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
    @DisplayName("GET /api/user/{page} retorna usuarios")
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
    @DisplayName("GET /api/user/{page} - Excepcion retornada por el service")
    void getUsers_exception() throws Exception {
        when(userService.search(2)).thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/user/2")).andExpect(status().is5xxServerError())
        .andExpect(content().string("Service Error"));
    }

    @Test
    @DisplayName("GET /api/user/{page} retorna usuarios con page seteado en 3")
    void getUsers_success_set_page() throws Exception {

        userApiResponse.setPage(3);
        when(userService.search(1)).thenReturn(userApiResponse);

        mockMvc.perform(get("/api/user/1")).andExpect((status().isOk()))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data").isArray()).andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath(("$.page")).value(3))
        .andExpect(jsonPath("$.total").value(2));
    }
}