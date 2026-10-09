package com.proyectoPPI.proyectodb.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoPPI.proyectodb.db.Db;
import com.proyectoPPI.proyectodb.security.RequireAdmin;
import com.proyectoPPI.proyectodb.security.RequireAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lecciones")
public class LeccionesController {

    private static final String LECCION_COLUMNS =
            "LeccionID, CursoID, Orden, Titulo, Tipo, Contenido, CodigoEjemplo, XPLeccion, FechaCreacion, Preguntas";

    private final Db db;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeccionesController(Db db) {
        this.db = db;
    }

    private void parsePreguntas(Map<String, Object> row) throws Exception {
        Object preguntas = row.get("Preguntas");
        if (preguntas instanceof String s && !s.isBlank()) {
            row.put("Preguntas", objectMapper.readValue(s, Object.class));
        } else {
            row.put("Preguntas", null);
        }
    }

    @GetMapping
    @RequireAdmin
    public ResponseEntity<?> getLecciones() {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + LECCION_COLUMNS + " FROM Lecciones ORDER BY CursoID, ISNULL(Orden, LeccionID)");
            for (Map<String, Object> row : rows) parsePreguntas(row);
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<?> getLeccionById(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + LECCION_COLUMNS + " FROM Lecciones WHERE LeccionID = ?", id);
            if (rows.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lección no encontrada"));
            Map<String, Object> leccion = rows.get(0);
            parsePreguntas(leccion);
            return ResponseEntity.ok(leccion);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping
    @RequireAdmin
    public ResponseEntity<?> createLeccion(@RequestBody Map<String, Object> body) throws Exception {
        Object cursoId = body.get("CursoID");
        String titulo = (String) body.get("Titulo");
        String tipo = (String) body.get("Tipo");

        if (cursoId == null || titulo == null || tipo == null) {
            return ResponseEntity.status(400).body(Map.of("error", "CursoID, Titulo y Tipo son requeridos"));
        }

        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            List<Map<String, Object>> cursoCheck = db.jdbc().queryForList(
                    "SELECT CursoID FROM Cursos WHERE CursoID = ?", cursoId);
            if (cursoCheck.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "El curso especificado no existe"));

            Integer orden = body.get("Orden") != null ? ((Number) body.get("Orden")).intValue() : 1;
            String contenido = (String) body.get("Contenido");
            String codigoEjemplo = (String) body.get("CodigoEjemplo");
            Integer xpLeccion = body.get("XPLeccion") != null ? ((Number) body.get("XPLeccion")).intValue() : 50;
            Object preguntas = body.get("Preguntas");
            String preguntasJson = preguntas != null ? objectMapper.writeValueAsString(preguntas) : null;
            Timestamp fechaCreacion = new Timestamp(System.currentTimeMillis());

            Integer leccionId = db.jdbc().queryForObject(
                    "INSERT INTO Lecciones (CursoID, Orden, Titulo, Tipo, Contenido, CodigoEjemplo, XPLeccion, FechaCreacion, Preguntas) " +
                            "OUTPUT INSERTED.LeccionID VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Integer.class,
                    cursoId, orden, titulo, tipo, contenido, codigoEjemplo, xpLeccion, fechaCreacion, preguntasJson
            );

            return ResponseEntity.status(201).body(Map.of("message", "Lección creada", "LeccionID", leccionId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> updateLeccion(@PathVariable int id, @RequestBody Map<String, Object> body) throws Exception {
        String titulo = (String) body.get("Titulo");
        String tipo = (String) body.get("Tipo");

        if (titulo == null || tipo == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Titulo y Tipo son requeridos"));
        }

        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            List<Map<String, Object>> leccionCheck = db.jdbc().queryForList(
                    "SELECT LeccionID FROM Lecciones WHERE LeccionID = ?", id);
            if (leccionCheck.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lección no encontrada"));

            Object cursoId = body.get("CursoID");
            if (cursoId != null) {
                List<Map<String, Object>> cursoCheck = db.jdbc().queryForList(
                        "SELECT CursoID FROM Cursos WHERE CursoID = ?", cursoId);
                if (cursoCheck.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "El curso especificado no existe"));
            }

            String contenido = (String) body.get("Contenido");
            String codigoEjemplo = (String) body.get("CodigoEjemplo");
            Integer xpLeccion = body.get("XPLeccion") != null ? ((Number) body.get("XPLeccion")).intValue() : 50;
            Object preguntas = body.get("Preguntas");
            String preguntasJson = preguntas != null ? objectMapper.writeValueAsString(preguntas) : null;
            Object orden = body.get("Orden");

            StringBuilder query = new StringBuilder(
                    "UPDATE Lecciones SET Titulo = ?, Tipo = ?, Contenido = ?, CodigoEjemplo = ?, XPLeccion = ?, Preguntas = ?");
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(titulo);
            params.add(tipo);
            params.add(contenido);
            params.add(codigoEjemplo);
            params.add(xpLeccion);
            params.add(preguntasJson);

            if (cursoId != null) {
                query.append(", CursoID = ?");
                params.add(cursoId);
            }
            if (orden != null) {
                query.append(", Orden = ?");
                params.add(((Number) orden).intValue());
            }
            query.append(" WHERE LeccionID = ?");
            params.add(id);

            db.jdbc().update(query.toString(), params.toArray());
            return ResponseEntity.ok(Map.of("message", "Lección actualizada"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> deleteLeccion(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            int rows = db.jdbc().update("DELETE FROM Lecciones WHERE LeccionID = ?", id);
            if (rows == 0) return ResponseEntity.status(404).body(Map.of("error", "Lección no encontrada"));
            return ResponseEntity.ok(Map.of("message", "Lección eliminada"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }
}
