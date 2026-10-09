package com.proyectoPPI.proyectodb.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link Db}. Se simula (mock) el JdbcTemplate para no
 * necesitar una base de datos real: cada prueba valida un comportamiento
 * distinto segun la respuesta simulada del JdbcTemplate.
 */
class DbTest {

    private JdbcTemplate jdbcTemplate;
    private Db db;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        db = new Db(jdbcTemplate);
    }

    @Test
    void jdbc_devuelveElJdbcTemplateInyectado() {
        assertThat(db.jdbc()).isSameAs(jdbcTemplate);
    }

    @Test
    void isAvailable_devuelveTrueCuandoLaVerificacionNoFalla() {
        boolean disponible = db.isAvailable();

        assertThat(disponible).isTrue();
        verify(jdbcTemplate).execute("SELECT 1");
    }

    @Test
    void isAvailable_devuelveFalseCuandoLaConexionFalla() {
        doThrow(new RuntimeException("timeout de conexion")).when(jdbcTemplate).execute("SELECT 1");

        boolean disponible = db.isAvailable();

        assertThat(disponible).isFalse();
    }

    @Test
    void hasColumn_devuelveTrueCuandoLaColumnaExiste() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Usuarios"), eq("Rol")))
                .thenReturn(1);

        assertThat(db.hasColumn("Usuarios", "Rol")).isTrue();
    }

    @Test
    void hasColumn_devuelveFalseCuandoLaColumnaNoExiste() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Usuarios"), eq("ColumnaFantasma")))
                .thenReturn(0);

        assertThat(db.hasColumn("Usuarios", "ColumnaFantasma")).isFalse();
    }

    @Test
    void hasColumn_devuelveFalseCuandoElConteoEsNulo() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Usuarios"), eq("Rol")))
                .thenReturn(null);

        assertThat(db.hasColumn("Usuarios", "Rol")).isFalse();
    }
}
