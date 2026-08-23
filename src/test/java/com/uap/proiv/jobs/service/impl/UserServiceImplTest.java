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

    @Test
    @DisplayName("search() delega en el repositorio pasando la pagina correcta")
    void search_delegaEnRepositorioConLaPaginaCorrecta() {
        UserApiResponse respuestaEsperada = new UserApiResponse();
        respuestaEsperada.setPage(2);
        when(userApiRepository.getUsers(2)).thenReturn(respuestaEsperada);

        UserApiResponse resultado = userService.search(2);

        assertThat(resultado).isSameAs(respuestaEsperada);
        verify(userApiRepository).getUsers(2);
    }

    @Test
    @DisplayName("searchById() delega en el repositorio y devuelve el usuario encontrado")
    void searchById_devuelveElUsuarioDelRepositorio() {
        User usuarioEsperado = new User();
        usuarioEsperado.setId(7);
        usuarioEsperado.setFirstName("Ana");
        when(userApiRepository.getUserById(7)).thenReturn(usuarioEsperado);

        User resultado = userService.searchById(7);

        assertThat(resultado).isSameAs(usuarioEsperado);
        verify(userApiRepository).getUserById(7);
    }
}
