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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeccionesControllerTest {

    private static final String PREGUNTAS_JSON = """
            [{"pregunta":"¿Qué etiqueta se usa para crear un enlace?",
              "opciones":["<link>","<a>","<url>","<href>"],
              "respuestaCorrecta":1,
              "explicacion":"La etiqueta <a> se usa para crear enlaces."}]
            """;

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

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getLeccionById_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(99))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getLeccionById(99);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
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

    @Test
    void buscarLecciones_devuelve400SiElTituloEstaVacio() {
        ResponseEntity<?> respuesta = controller.buscarLecciones("");

        assertEquals(400, respuesta.getStatusCode().value());
    }

    @Test
    void buscarLecciones_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.buscarLecciones("html");

        assertEquals(503, respuesta.getStatusCode().value());
    }

    @Test
    void buscarLecciones_devuelve200ConLasLeccionesQueCoinciden() {
        when(db.isAvailable()).thenReturn(true);
        Map<String, Object> leccion = new HashMap<>(); // mutable: parsePreguntas hace put()
        leccion.put("LeccionID", 7);
        leccion.put("Titulo", "¿Qué es HTML?");
        leccion.put("Preguntas", null);
        when(jdbcTemplate.queryForList(anyString(), eq("%html%"))).thenReturn(List.of(leccion));

        ResponseEntity<?> respuesta = controller.buscarLecciones("html");

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(1, ((List<?>) respuesta.getBody()).size());
    }

    @Test
    void verificarRespuesta_devuelve404SiLaLeccionNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(99))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.verificarRespuesta(99, Map.of("PreguntaIndex", 0, "Respuesta", 1));

        assertEquals(404, respuesta.getStatusCode().value());
    }

    @Test
    void verificarRespuesta_devuelveCorrectaTrueCuandoCoincideConLaRespuestaVerdadera() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(12)))
                .thenReturn(List.of(Map.of("Preguntas", PREGUNTAS_JSON)));

        ResponseEntity<?> respuesta = controller.verificarRespuesta(12, Map.of("PreguntaIndex", 0, "Respuesta", 1));

        assertEquals(200, respuesta.getStatusCode().value());
        Map<?, ?> cuerpo = (Map<?, ?>) respuesta.getBody();
        assertEquals(true, cuerpo.get("Correcta"));
        assertEquals(1, cuerpo.get("RespuestaCorrecta"));
    }

    @Test
    void verificarRespuesta_devuelveCorrectaFalseCuandoLaRespuestaEsIncorrecta() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(12)))
                .thenReturn(List.of(Map.of("Preguntas", PREGUNTAS_JSON)));

        ResponseEntity<?> respuesta = controller.verificarRespuesta(12, Map.of("PreguntaIndex", 0, "Respuesta", 3));

        assertEquals(200, respuesta.getStatusCode().value());
        Map<?, ?> cuerpo = (Map<?, ?>) respuesta.getBody();
        assertEquals(false, cuerpo.get("Correcta"));
        assertEquals(1, cuerpo.get("RespuestaCorrecta"));
    }
}