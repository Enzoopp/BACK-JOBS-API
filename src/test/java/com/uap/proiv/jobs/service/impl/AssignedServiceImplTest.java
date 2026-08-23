package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.dto.AssignedResponse;
import com.uap.proiv.jobs.dto.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AssignedServiceImpl")
class AssignedServiceImplTest {

    private final AssignedServiceImpl assignedService = new AssignedServiceImpl();

    private Job crearJob(int id, int resources) {
        Job job = new Job();
        job.setId(id);
        job.setResources(resources);
        return job;
    }

    @Test
    @DisplayName("create() genera tantas asignaciones por job como recursos (resources) pida ese job")
    void create_generaUnaAsignacionPorCadaRecursoDelJob() {
        Job job = crearJob(10, 3);
        List<Integer> userIds = new ArrayList<>(List.of(1, 2, 3, 4, 5));

        List<AssignedResponse> resultado = assignedService.create(List.of(job), userIds);

        assertThat(resultado).hasSize(3);
        assertThat(resultado).allMatch(a -> a.jobId() == 10);
    }

    @Test
    @DisplayName("create() usa siempre ids de usuario que estan dentro de la lista recibida")
    void create_asignaSoloUsuariosDeLaListaRecibida() {
        Job job = crearJob(10, 2);
        List<Integer> userIds = new ArrayList<>(List.of(100, 200, 300));

        List<AssignedResponse> resultado = assignedService.create(List.of(job), userIds);

        assertThat(resultado).extracting(AssignedResponse::userId)
                .allMatch(userIds::contains);
    }

    @Test
    @DisplayName("create() suma las asignaciones de todos los jobs recibidos")
    void create_sumaAsignacionesDeVariosJobs() {
        Job job1 = crearJob(1, 2);
        Job job2 = crearJob(2, 1);
        List<Integer> userIds = new ArrayList<>(List.of(1, 2, 3));

        List<AssignedResponse> resultado = assignedService.create(List.of(job1, job2), userIds);

        assertThat(resultado).hasSize(3);
        assertThat(resultado).filteredOn(a -> a.jobId() == 1).hasSize(2);
        assertThat(resultado).filteredOn(a -> a.jobId() == 2).hasSize(1);
    }

    @Test
    @DisplayName("create() con lista vacia de jobs devuelve una lista vacia")
    void create_conListaDeJobsVaciaDevuelveListaVacia() {
        List<AssignedResponse> resultado = assignedService.create(List.of(), new ArrayList<>(List.of(1, 2)));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("create() explota si un job pide mas recursos que usuarios disponibles")
    void create_lanzaExcepcionSiFaltanUsuariosDisponibles() {
        Job job = crearJob(1, 5);
        List<Integer> userIds = new ArrayList<>(List.of(1, 2));

        assertThatThrownBy(() -> assignedService.create(List.of(job), userIds))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }
}
