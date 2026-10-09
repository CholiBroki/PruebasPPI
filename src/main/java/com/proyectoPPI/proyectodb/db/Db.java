package com.proyectoPPI.proyectodb.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Db {

    private final JdbcTemplate jdbcTemplate;

    public Db(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate jdbc() {
        return jdbcTemplate;
    }

    public boolean isAvailable() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return true;
        } catch (Exception error) {
            System.out.println("Error en la conexion: " + error.getMessage());
            return false;
        }
    }

    public boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, tableName, columnName
        );
        return count != null && count > 0;
    }
}
