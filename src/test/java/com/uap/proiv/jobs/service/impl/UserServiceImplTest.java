package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.client.UserApiRepository;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserApiRepository userApiRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userApiRepository);
    }

    // ---------- search() ----------

    @Test
    @DisplayName("search() - Caso de exito: delega en el repositorio pasando la pagina correcta")
    void search_delegaEnRepositorioConLaPaginaCorrecta() {
        UserApiResponse respuestaEsperada = new UserApiResponse();
        respuestaEsperada.setPage(2);
        when(userApiRepository.getUsers(2)).thenReturn(respuestaEsperada);

        UserApiResponse resultado = userService.search(2);

        assertThat(resultado).isSameAs(respuestaEsperada);
        verify(userApiRepository).getUsers(2);
    }

    @Test
    @DisplayName("search() - Camino de excepcion: propaga la excepcion del repositorio tal cual")
    void search_propagaExcepcionDelRepositorio() {
        when(userApiRepository.getUsers(2))
                .thenThrow(new RuntimeException("Error en ReqRes API. Codigo: 500"));

        assertThatThrownBy(() -> userService.search(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error en ReqRes API. Codigo: 500");

        verify(userApiRepository).getUsers(2);
    }

    // ---------- searchById() ----------

    @Test
    @DisplayName("searchById() - Caso de exito: devuelve el usuario encontrado por el repositorio")
    void searchById_devuelveElUsuarioDelRepositorio() {
        User usuarioEsperado = new User();
        usuarioEsperado.setId(7);
        usuarioEsperado.setFirstName("Ana");
        when(userApiRepository.getUserById(7)).thenReturn(usuarioEsperado);

        User resultado = userService.searchById(7);

        assertThat(resultado).isSameAs(usuarioEsperado);
        verify(userApiRepository).getUserById(7);
    }

    @Test
    @DisplayName("searchById() - Camino de excepcion: propaga la excepcion del repositorio tal cual")
    void searchById_propagaExcepcionDelRepositorio() {
        when(userApiRepository.getUserById(99))
                .thenThrow(new RuntimeException("Error en ReqRes API. Codigo: 404"));

        assertThatThrownBy(() -> userService.searchById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error en ReqRes API. Codigo: 404");

        verify(userApiRepository).getUserById(99);
    }

    // ---------- update() ----------

    @Test
    @DisplayName("update() - Caso de exito: delega la actualizacion en el repositorio sin lanzar excepcion")
    void update_delegaEnRepositorioSinLanzarExcepcion() {
        User user = new User();
        user.setId(3);
        user.setFirstName("Juan");
        user.setLastName("Perez");

        userService.update(user);

        verify(userApiRepository).updateUser(user);
    }

    @Test
    @DisplayName("update() - Camino de excepcion: envuelve la excepcion del repositorio en una RuntimeException propia")
    void update_envuelveExcepcionDelRepositorio() {
        User user = new User();
        user.setId(3);

        doThrow(new RuntimeException("Error en ReqRes API. Codigo: 500"))
                .when(userApiRepository).updateUser(any(User.class));

        assertThatThrownBy(() -> userService.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error al crear el usuario: Error en ReqRes API. Codigo: 500")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(userApiRepository).updateUser(user);
    }
}
