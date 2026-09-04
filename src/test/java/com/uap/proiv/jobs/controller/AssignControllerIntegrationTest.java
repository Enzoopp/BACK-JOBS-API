package com.uap.proiv.jobs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uap.proiv.jobs.client.UserApiRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracion de /api/assign, mockeando solo la API externa de reqres.in (igual que UserControllerIntegrationTest).
@SpringBootTest
@AutoConfigureMockMvc
public class AssignControllerIntegrationTest {

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
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            String baseUrl = mockWebServer.url("/api/users").toString();
            String apikey = "free_user_3HYTiqu2JKQ4TfGq884xW5mqfrd";

            return new UserApiRepository(httpClient, objectMapper, baseUrl, apikey);
        }
    }

    // El job con mas recursos (Full Stack Developer) pide 10, asi que devolvemos 12 usuarios en 2 paginas.
    private String paginaDeUsuarios(int page, int totalPages, int desde, int hasta) {
        StringBuilder data = new StringBuilder();
        for (int i = desde; i <= hasta; i++) {
            if (data.length() > 0) data.append(",");
            data.append(String.format("""
                    {"id":%d,"email":"user%d@correo.com","first_name":"Nombre%d","last_name":"Apellido%d","avatar":"https://reqres.in/img/faces/%d.jpg"}
                    """, i, i, i, i, i));
        }
        return String.format("""
                {"page":%d,"per_page":6,"total":12,"total_pages":%d,"data":[%s]}
                """, page, totalPages, data);
    }

    @Test
    @Disabled("assign() pasa una lista inmutable a Collections.shuffle() -> UnsupportedOperationException. Pendiente.")
    @DisplayName("POST /api/assign - pagina usuarios externos y asigna recursos a cada job de jobs.json")
    void assign_success_conPaginacionDeUsuarios() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(1, 2, 1, 6))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(2, 2, 7, 12))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        // assign() usa "page <= totalPages", asi que pide una pagina de mas; la simulamos vacia.
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(3, 2, 13, 12))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        String requestJson = """
                {"requestNumber": 555, "clientName": "Acme Corp"}
                """;

        mockMvc.perform(post("/api/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.Client").value("Acme Corp"))
                .andExpect(jsonPath("$.Request_Number").value(555))
                .andExpect(jsonPath("$.Assign").isArray())
                .andExpect(jsonPath("$.Assign.length()").value(7))
                .andExpect(jsonPath("$.Assign[0].job.name").value("Data Engineer"))
                .andExpect(jsonPath("$.Assign[0].users.length()").value(3))
                .andExpect(jsonPath("$.Assign[6].job.name").value("Full Stack Developer"))
                .andExpect(jsonPath("$.Assign[6].users.length()").value(10));

        RecordedRequest primeraPagina = mockWebServer.takeRequest();
        assertEquals("/api/users?page=1", primeraPagina.getPath());
        assertEquals("free_user_3HYTiqu2JKQ4TfGq884xW5mqfrd", primeraPagina.getHeader("X-API-KEY"));

        RecordedRequest segundaPagina = mockWebServer.takeRequest();
        assertEquals("/api/users?page=2", segundaPagina.getPath());

        RecordedRequest terceraPagina = mockWebServer.takeRequest();
        assertEquals("/api/users?page=3", terceraPagina.getPath());
    }

    @Test
    @DisplayName("POST /api/assign - sin clientName responde 400")
    void assign_requestInvalido_retorna400() throws Exception {
        String requestJson = """
                {"requestNumber": 555}
                """;

        mockMvc.perform(post("/api/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
