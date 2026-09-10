package com.uap.proiv.jobs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.client.UserApiRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test de Integracion
// Se tiene que levantar todo el contexto

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserApiRepository userApiRepository;

    static MockWebServer mockWebServer;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.close();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public UserApiRepository userApiRepository(ObjectMapper objectMapper) {
            HttpClient httpClient = HttpClient.newBuilder() // Simulamos la llamada a la API externa
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            String baseUrl = mockWebServer.url("/api/users").toString();
            String apikey = "free_user_3HYTiqu2JKQ4TfGq884xW5mqfrd";

            return new UserApiRepository(httpClient, objectMapper, baseUrl, apikey);
        }
    }

    @Test
    @DisplayName("Get api/user/id/{id} integracion UserController, UserService, UserApiRepository con mock de la API externa")
    void getUserById() throws Exception {
        String jsonResponse = """
                {
                                    "page": 1,
                                    "data": [{
                                        "id": 2,
                                        "email": "john.doe@example.com",
                                        "first_name": "Juan",
                                        "last_name": "Perez",
                                        "avatar": "https://reqres.in/img/faces/2.jpg"
                                    }]
                }
                """;

        mockWebServer.enqueue(new MockResponse() // Mockea lo externo
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        );

        mockMvc.perform(get("/api/user/id/2")) // Es mi llamada a la API interna
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.first_name").value("Juan"))
                .andExpect(jsonPath("$.last_name").value("Perez"))
                .andExpect(jsonPath("$.avatar").value("https://reqres.in/img/faces/2.jpg"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals("free_user_3HYTiqu2JKQ4TfGq884xW5mqfrd", request.getHeader("X-API-KEY"));
    }

    @Test
    @DisplayName("Post /api/user/update integracion UserController, UserService, UserApiRepository con mock de la API externa")
    void updateUser() throws Exception {
        String updateResponse = """
                {
                    "name":"Juan",
                    "job":"Garcia",
                    "updatedAt":"2024-01-01T12:00:00.000Z"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(updateResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        );

        String userJson = """
                {
                    "id":2,
                    "first_name":"Juan",
                    "last_name":"Garcia"
                }
                """;

        mockMvc.perform(post("/api/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/api/users/2", request.getPath());
        assertEquals("application/json", request.getHeader("Accept"));
        assertEquals("free_user_3HYTiqu2JKQ4TfGq884xW5mqfrd", request.getHeader("X-API-KEY"));
    }
}
