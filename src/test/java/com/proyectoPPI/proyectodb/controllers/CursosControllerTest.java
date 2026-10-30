package com.proyectoPPI.proyectodb.controllers;

import com.proyectoPPI.proyectodb.db.Db;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CursosControllerTest {

    private Db db;
    private JdbcTemplate jdbcTemplate;
    private CursosController controller;

    @BeforeEach
    void setUp() {
        db = mock(Db.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        when(db.jdbc()).thenReturn(jdbcTemplate);
        controller = new CursosController(db);
    }

    @Test
    void getCursos_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.getCursos();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getCursos_devuelve200ConLaListaDeCursosPublicados() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("CursoID", 1)));

        ResponseEntity<?> respuesta = controller.getCursos();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) respuesta.getBody()).hasSize(1);
    }

    @Test
    void getCursos_devuelve500SiOcurreUnaExcepcion() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("error sql"));

        ResponseEntity<?> respuesta = controller.getCursos();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void getAllCursos_devuelve200ConTodosLosCursosSinFiltrar() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(List.of(Map.of("CursoID", 1), Map.of("CursoID", 2)));

        ResponseEntity<?> respuesta = controller.getAllCursos();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) respuesta.getBody()).hasSize(2);
    }

    @Test
    void getCursoById_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(99))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getCursoById(99);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getCursoById_devuelve200SiElCursoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(1)))
                .thenReturn(List.of(Map.of("CursoID", 1, "Titulo", "Java")));

        ResponseEntity<?> respuesta = controller.getCursoById(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void createCurso_devuelve400SiFaltaElTitulo() {
        ResponseEntity<?> respuesta = controller.createCurso(Map.of("InstructorID", 1));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void createCurso_devuelve400SiFaltaElInstructorID() {
        ResponseEntity<?> respuesta = controller.createCurso(Map.of("Titulo", "Java Basico"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void createCurso_devuelve201YElIdGeneradoConDatosValidos() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(10);

        Map<String, Object> body = new HashMap<>();
        body.put("Titulo", "Java Basico");
        body.put("InstructorID", 1);

        ResponseEntity<?> respuesta = controller.createCurso(body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Map<?, ?> cuerpo = (Map<?, ?>) respuesta.getBody();
        assertThat(cuerpo.get("CursoID")).isEqualTo(10);
    }

    @Test
    void createCurso_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        Map<String, Object> body = new HashMap<>();
        body.put("Titulo", "Java Basico");
        body.put("InstructorID", 1);

        ResponseEntity<?> respuesta = controller.createCurso(body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getLeccionesByCurso_devuelve404SiElCursoNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(5))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getLeccionesByCurso(5);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getLeccionesByCurso_devuelve403SiElCursoEstaInactivo() {
        when(db.isAvailable()).thenReturn(true);
        Map<String, Object> curso = new HashMap<>();
        curso.put("CursoID", 5);
        curso.put("Activo", false);
        when(jdbcTemplate.queryForList(anyString(), eq(5))).thenReturn(List.of(curso));

        ResponseEntity<?> respuesta = controller.getLeccionesByCurso(5);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void getLeccionesByCurso_devuelve200ConCursoYLeccionesCuandoElCursoEstaActivo() {
        when(db.isAvailable()).thenReturn(true);
        Map<String, Object> curso = new HashMap<>();
        curso.put("CursoID", 5);
        curso.put("Activo", true);
        when(jdbcTemplate.queryForList(anyString(), eq(5)))
                .thenReturn(List.of(curso))
                .thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getLeccionesByCurso(5);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        
        @SuppressWarnings("unchecked")
        Map<String, ?> cuerpo = (Map<String, ?>) respuesta.getBody();
        assertThat(cuerpo).containsKeys("curso", "lecciones");
    }

    @Test
    void deleteCurso_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.update(anyString(), eq(123))).thenReturn(0);

        ResponseEntity<?> respuesta = controller.deleteCurso(123);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deleteCurso_devuelve200SiSeEliminoCorrectamente() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

        ResponseEntity<?> respuesta = controller.deleteCurso(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void buscarPorTitulo_devuelve400SiElTituloEstaVacio() {
        ResponseEntity<?> respuesta = controller.buscarPorTitulo("   ");

        assertEquals(400, respuesta.getStatusCode().value());
    }

    @Test
    void buscarPorTitulo_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.buscarPorTitulo("java");

        assertEquals(503, respuesta.getStatusCode().value());
    }

    @Test
    void buscarPorTitulo_devuelve200ConLosCursosQueCoinciden() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq("%java%")))
                .thenReturn(List.of(Map.of("CursoID", 5, "Titulo", "Curso Completo de Java")));

        ResponseEntity<?> respuesta = controller.buscarPorTitulo("java");

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
    }
}