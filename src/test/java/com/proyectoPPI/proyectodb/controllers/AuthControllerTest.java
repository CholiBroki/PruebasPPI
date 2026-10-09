package com.proyectoPPI.proyectodb.controllers;

import com.proyectoPPI.proyectodb.db.Db;
import com.proyectoPPI.proyectodb.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para el endpoint /api/auth/login. Se simulan Db y
 * JwtUtil para poder validar cada rama del metodo login() sin necesitar
 * una base de datos real, segun el dato (usuario/contrasena) recibido.
 */
class AuthControllerTest {

    private Db db;
    private JdbcTemplate jdbcTemplate;
    private JwtUtil jwtUtil;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        db = mock(Db.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        jwtUtil = mock(JwtUtil.class);
        when(db.jdbc()).thenReturn(jdbcTemplate);

        controller = new AuthController(db, jwtUtil);
        ReflectionTestUtils.setField(controller, "adminUsername", "admin");
        ReflectionTestUtils.setField(controller, "adminEmail", "admin@admin.com");
    }

    private Map<String, Object> usuarioBase(String hash, String salt, boolean activo, String rol) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("UsuarioID", 1);
        usuario.put("NombreUsuario", "juan");
        usuario.put("CorreoElectronico", "juan@test.com");
        usuario.put("PasswordHash", hash);
        usuario.put("PasswordSalt", salt);
        usuario.put("Activo", activo);
        usuario.put("EmailVerificado", true);
        usuario.put("Rol", rol);
        return usuario;
    }

    @Test
    void login_devuelve400SiFaltaNombreUsuario() {
        ResponseEntity<?> respuesta = controller.login(Map.of("Contrasena", "123"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void login_devuelve400SiElCuerpoDeLaPeticionEsNulo() {
        ResponseEntity<?> respuesta = controller.login(null);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void login_devuelve503SiLaBaseDeDatosNoEstaDisponible() {
        when(db.isAvailable()).thenReturn(false);

        ResponseEntity<?> respuesta = controller.login(Map.of("NombreUsuario", "juan", "Contrasena", "123"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void login_devuelve401SiElUsuarioNoExiste() {
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());

        ResponseEntity<?> respuesta = controller.login(Map.of("NombreUsuario", "noexiste", "Contrasena", "123"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void login_devuelve403SiElUsuarioEstaInactivo() {
        String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(usuarioBase(hash, "", false, "user")));

        ResponseEntity<?> respuesta = controller.login(Map.of("NombreUsuario", "juan", "Contrasena", "123456"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void login_devuelve401SiLaContrasenaEsIncorrecta() {
        String hash = BCrypt.hashpw("correcta", BCrypt.gensalt());
        when(db.isAvailable()).thenReturn(true);
        when(db.hasColumn("Usuarios", "Rol")).thenReturn(true);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(usuarioBase(hash, "", true, "user")));

        ResponseEntity<?> respuesta = controller.login(Map.of("NombreUsuario", "juan", "Contrasena", "incorrecta"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void login_devuelve500SiOcurreUnaExcepcionInesperada() {
        when(db.isAvailable()).thenThrow(new RuntimeException("fallo de conexion"));

        ResponseEntity<?> respuesta = controller.login(Map.of("NombreUsuario", "juan", "Contrasena", "123"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
    }

}
