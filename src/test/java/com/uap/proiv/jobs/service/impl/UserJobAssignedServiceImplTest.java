package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.dto.AssignedResponse;
import com.uap.proiv.jobs.dto.Job;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.AssignedService;
import com.uap.proiv.jobs.service.JobService;
import com.uap.proiv.jobs.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserJobAssignedServiceImpl")
class UserJobAssignedServiceImplTest {

    @Mock
    private JobService jobService;

    @Mock
    private UserService userService;

    @Mock
    private AssignedService assignedService;

    @InjectMocks
    private UserJobAssignedServiceImpl userJobAssignedService;

    private Job job(int id, int resources) {
        Job job = new Job();
        job.setId(id);
        job.setResources(resources);
        return job;
    }

    private User user(int id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private UserApiResponse pagina(int page, int totalPages, List<User> data) {
        UserApiResponse response = new UserApiResponse();
        response.setPage(page);
        response.setTotalPages(totalPages);
        response.setData(data);
        return response;
    }

    @Test
    @DisplayName("assign() arma un UserJobAssigned por cada job, con los usuarios que le asigno AssignedService")
    void assign_combinaJobsConSusUsuariosAsignados() {
        Job job1 = job(1, 1);
        User userA = user(100);

        when(jobService.getAllJobs()).thenReturn(List.of(job1));
        when(userService.search(1)).thenReturn(pagina(1, 1, List.of(userA)));
        when(assignedService.create(List.of(job1), List.of(100)))
                .thenReturn(List.of(new AssignedResponse(1, 100)));

        List<UserJobAssigned> resultado = userJobAssignedService.assign();

        assertThat(resultado).hasSize(1);
        UserJobAssigned asignacionJob1 = resultado.get(0);
        assertThat(asignacionJob1.getJob()).isEqualTo(job1);
        assertThat(asignacionJob1.getUsers()).containsExactly(userA);
    }

    @Test
    @DisplayName("assign() recorre todas las paginas de usuarios antes de pedir las asignaciones")
    void assign_recorreTodasLasPaginasDeUsuarios() {
        Job job1 = job(1, 2);
        User userA = user(100);
        User userB = user(200);

        when(jobService.getAllJobs()).thenReturn(List.of(job1));
        when(userService.search(1)).thenReturn(pagina(1, 2, List.of(userA)));
        when(userService.search(2)).thenReturn(pagina(2, 2, List.of(userB)));
        when(assignedService.create(eq(List.of(job1)), eq(List.of(100, 200))))
                .thenReturn(List.of(
                        new AssignedResponse(1, 100),
                        new AssignedResponse(1, 200)
                ));

        List<UserJobAssigned> resultado = userJobAssignedService.assign();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUsers()).containsExactlyInAnyOrder(userA, userB);

        verify(userService, times(1)).search(1);
        verify(userService, times(1)).search(2);
        verify(userService, times(1)).search(3);
    }

    @Test
    @DisplayName("assign() devuelve lista vacia si no hay jobs")
    void assign_sinJobsDevuelveListaVacia() {
        when(jobService.getAllJobs()).thenReturn(List.of());
        when(userService.search(1)).thenReturn(pagina(1, 1, List.of()));
        when(assignedService.create(List.of(), List.of())).thenReturn(List.of());

        List<UserJobAssigned> resultado = userJobAssignedService.assign();

        assertThat(resultado).isEmpty();
    }
}
