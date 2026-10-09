package com.proyectoPPI.proyectodb.controllers;

import com.proyectoPPI.proyectodb.db.Db;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para UsuariosController, incluyendo el metodo
 * seedAdminUser() que se ejecuta al arrancar la aplicacion.
 */
class UsuariosControllerTest {

    private Db db;
    private JdbcTemplate jdbcTemplate;
    private UsuariosController controller;

    @BeforeEach
    void setUp() {
        db = mock(Db.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        when(db.jdbc()).thenReturn(jdbcTemplate);
        controller = new UsuariosController(db);

        ReflectionTestUtils.setField(controller, "adminUsername", "admin");
        ReflectionTestUtils.setField(controller, "adminEmail", "admin@admin.com");
        ReflectionTestUtils.setField(controller, "adminPassword", "Admin123!2026");
        ReflectionTestUtils.setField(controller, "adminNombre", "Administrador");
        ReflectionTestUtils.setField(controller, "adminApellido", "Sistema");
    }

    @Test
    void getUsuarios_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.getUsuarios();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getUsuarios_devuelve200ConLaListaDeUsuarios() {
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("UsuarioID", 1)));

        ResponseEntity<?> respuesta = controller.getUsuarios();

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) respuesta.getBody()).hasSize(1);
    }

    @Test
    void getUsuarioById_devuelve404SiNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq(5))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.getUsuarioById(5);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getUsuarioById_devuelve200SiExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(false);
        when(jdbcTemplate.queryForList(anyString(), eq(1)))
                .thenReturn(List.of(Map.of("UsuarioID", 1, "NombreUsuario", "juan")));

        ResponseEntity<?> respuesta = controller.getUsuarioById(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void createUsuario_devuelve400SiFaltanCamposObligatorios() {
        ResponseEntity<?> respuesta = controller.createUsuario(Map.of("NombreUsuario", "juan"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void createUsuario_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        Map<String, Object> body = new HashMap<>();
        body.put("NombreUsuario", "juan");
        body.put("CorreoElectronico", "juan@test.com");
        body.put("Contrasena", "clave123");

        ResponseEntity<?> respuesta = controller.createUsuario(body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void createUsuario_devuelve201YElIdGeneradoConDatosValidos() {
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(7);

        Map<String, Object> body = new HashMap<>();
        body.put("NombreUsuario", "juan");
        body.put("CorreoElectronico", "juan@test.com");
        body.put("Contrasena", "clave123");

        ResponseEntity<?> respuesta = controller.createUsuario(body);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Map<?, ?> cuerpo = (Map<?, ?>) respuesta.getBody();
        assertThat(cuerpo.get("UsuarioID")).isEqualTo(7);
    }

    @Test
    void deleteUsuario_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.deleteUsuario(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void deleteUsuario_devuelve200CuandoLaOperacionSeCompleta() {
        when(db.isAvailable()).thenReturn(true);

        ResponseEntity<?> respuesta = controller.deleteUsuario(1);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void seedAdminUser_noHaceNadaSiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        controller.seedAdminUser();

        verify(jdbcTemplate, never()).queryForList(anyString(), any(Object[].class));
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void seedAdminUser_noCreaUnDuplicadoSiElAdminYaExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq("admin"), eq("admin@admin.com")))
                .thenReturn(List.of(Map.of("UsuarioID", 1)));

        controller.seedAdminUser();

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void seedAdminUser_creaElUsuarioAdminSiAunNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), eq("admin"), eq("admin@admin.com")))
                .thenReturn(List.of());
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);

        controller.seedAdminUser();

        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }
}
