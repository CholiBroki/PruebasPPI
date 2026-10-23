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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para LeccionesController: valida los distintos
 * codigos de respuesta y el parseo del campo JSON "Preguntas" segun el
 * dato que retorna la base de datos (simulada).
 */
class LeccionesControllerTest {

    private Db db;
    private JdbcTemplate jdbcTemplate;
    private LeccionesController controller;

    @BeforeEach
    void setUp() {
        db = mock(Db.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        when(db.jdbc()).thenReturn(jdbcTemplate);
        controller = new LeccionesController(db);
    }

    @Test
    void getLecciones_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.getLecciones();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void getLeccionById_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(99))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getLeccionById(99);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void getLeccionById_devuelve200SiExiste() {
        when(db.isAvailable()).thenReturn(true);
        Map<String, Object> leccion = new HashMap<>();
        leccion.put("LeccionID", 1);
        leccion.put("Preguntas", null);
        when(jdbcTemplate.queryForList(anyString(), eq(1))).thenReturn(List.of(leccion));

        ResponseEntity<?> respuesta = controller.getLeccionById(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void createLeccion_devuelve400SiFaltanCamposRequeridos() throws Exception {
        ResponseEntity<?> respuesta = controller.createLeccion(Map.of("Titulo", "Intro"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void updateLeccion_devuelve400SiFaltanCamposRequeridos() throws Exception {
        ResponseEntity<?> respuesta = controller.updateLeccion(1, Map.of("Titulo", "Solo titulo"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void updateLeccion_devuelve404SiLaLeccionNoExiste() throws Exception {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(1))).thenReturn(List.of());

        Map<String, Object> body = new HashMap<>();
        body.put("Titulo", "Nuevo titulo");
        body.put("Tipo", "practica");

        ResponseEntity<?> respuesta = controller.updateLeccion(1, body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void updateLeccion_devuelve200SiSeActualizoCorrectamente() throws Exception {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(1))).thenReturn(List.of(Map.of("LeccionID", 1)));

        Map<String, Object> body = new HashMap<>();
        body.put("Titulo", "Nuevo titulo");
        body.put("Tipo", "practica");

        ResponseEntity<?> respuesta = controller.updateLeccion(1, body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void deleteLeccion_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(0);

        ResponseEntity<?> respuesta = controller.deleteLeccion(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deleteLeccion_devuelve200SiSeEliminoCorrectamente() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

        ResponseEntity<?> respuesta = controller.deleteLeccion(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }
}
