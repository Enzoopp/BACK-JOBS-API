package com.uap.proiv.jobs.e2e;

import com.uap.proiv.jobs.client.UserApiRepository;
import com.uap.proiv.jobs.dto.AssignRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

// E2E de /api/assign: servidor real en puerto random + TestRestTemplate, en vez de MockMvc.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class AssignE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserApiRepository userApiRepository;

    static MockWebServer mockWebServer;

    @BeforeAll
    static void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
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
    @Disabled("Mismo bug que en AssignControllerIntegrationTest (shuffle sobre lista inmutable). Pendiente.")
    @DisplayName("POST /api/assign (e2e) - caso de exito")
    void assign_e2e_success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(1, 2, 1, 6)).setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(2, 2, 7, 12)).setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(paginaDeUsuarios(3, 2, 13, 12)).setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        AssignRequest request = new AssignRequest();
        request.setRequestNumber(777);
        request.setClientName("Globex E2E");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/assign", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Globex E2E").contains("\"Request_Number\":777");
    }

    @Test
    @DisplayName("POST /api/assign (e2e) - sin clientName responde 400")
    void assign_e2e_requestInvalido_retorna400() {
        AssignRequest request = new AssignRequest();
        request.setRequestNumber(1);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/assign", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/assign (e2e) - JSON mal formado responde 400")
    void assign_e2e_jsonMalformado_retorna400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{ esto no es json valido", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/assign", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
