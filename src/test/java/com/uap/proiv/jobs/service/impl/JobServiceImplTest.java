package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.client.JobApiRepository;
import com.uap.proiv.jobs.dto.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobServiceImpl")
class JobServiceImplTest {

    @Mock
    private JobApiRepository jobApiRepository;

    private JobServiceImpl jobService;

    private Job job1;
    private Job job2;

    @BeforeEach
    void setUp() {
        jobService = new JobServiceImpl(jobApiRepository);

        job1 = new Job();
        job1.setId(1);
        job1.setName("Desarrollador Backend");
        job1.setSalary(1500.0);
        job1.setHours(40);
        job1.setResources(2);

        job2 = new Job();
        job2.setId(2);
        job2.setName("QA Analyst");
        job2.setSalary(1200.0);
        job2.setHours(30);
        job2.setResources(1);
    }

    @Test
    @DisplayName("getAllJobs() devuelve la lista completa que entrega el repositorio")
    void getAllJobs_devuelveListaDelRepositorio() {
        List<Job> jobsEsperados = List.of(job1, job2);
        when(jobApiRepository.getAllJobs()).thenReturn(jobsEsperados);

        List<Job> resultado = jobService.getAllJobs();

        assertThat(resultado).isEqualTo(jobsEsperados);
        verify(jobApiRepository).getAllJobs();
    }

    @Test
    @DisplayName("getJobById() devuelve el job cuyo id coincide")
    void getJobById_encuentraElJobCorrecto() {
        when(jobApiRepository.getAllJobs()).thenReturn(List.of(job1, job2));

        Job resultado = jobService.getJobById(2);

        assertThat(resultado).isEqualTo(job2);
    }

    @Test
    @DisplayName("getJobById() lanza una excepcion si no existe ningun job con ese id")
    void getJobById_lanzaExcepcionSiNoExiste() {
        when(jobApiRepository.getAllJobs()).thenReturn(List.of(job1, job2));

        assertThatThrownBy(() -> jobService.getJobById(999))
                .isInstanceOf(NoSuchElementException.class);
    }
}
