package com.proyectoPPI.proyectodb.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de {@link JwtUtil}. No requieren Spring ni base de
 * datos: se construye la clase directamente con un secreto de prueba.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("clave-secreta-de-prueba-para-los-tests-unitarios");
    }

    @Test
    void generateToken_creaUnTokenJwtConTresSegmentos() {
        String token = jwtUtil.generateToken(1, "usuario1", "correo@test.com", "user");

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

}
