package com.proyectoPPI.proyectodb.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoPPI.proyectodb.db.Db;
import com.proyectoPPI.proyectodb.security.RequireAdmin;
import com.proyectoPPI.proyectodb.security.RequireAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cursos")
public class CursosController {

    private static final String CURSO_COLUMNS =
            "CursoID, Titulo, Descripcion, NivelDificultad, ImagenPortada, Idioma, " +
                    "EstaPublicado, Activo, FechaCreacion, FechaPublicacion, XPRecompensa, InstructorID";

    private final Db db;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CursosController(Db db) {
        this.db = db;
    }

    @GetMapping
    public ResponseEntity<?> getCursos() {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + CURSO_COLUMNS + " FROM Cursos WHERE EstaPublicado = 1 ORDER BY FechaCreacion DESC");
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/all")
    @RequireAdmin
    public ResponseEntity<?> getAllCursos() {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + CURSO_COLUMNS + " FROM Cursos ORDER BY FechaCreacion DESC");
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    // NUEVO: busca cursos publicados cuyo titulo contenga el texto recibido
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorTitulo(@RequestParam("titulo") String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "El titulo es requerido"));
        }
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + CURSO_COLUMNS + " FROM Cursos WHERE EstaPublicado = 1 AND Titulo LIKE ? ORDER BY FechaCreacion DESC",
                    "%" + titulo.trim() + "%");
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCursoById(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + CURSO_COLUMNS + " FROM Cursos WHERE CursoID = ?", id);
            if (rows.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));
            return ResponseEntity.ok(rows.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/{id}/lecciones")
    @RequireAuth
    public ResponseEntity<?> getLeccionesByCurso(@PathVariable("id") int cursoId) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            List<Map<String, Object>> cursoRows = db.jdbc().queryForList(
                    "SELECT " + CURSO_COLUMNS + " FROM Cursos WHERE CursoID = ?", cursoId);
            if (cursoRows.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));

            Map<String, Object> curso = cursoRows.get(0);
            if (!Boolean.TRUE.equals(curso.get("Activo"))) {
                return ResponseEntity.status(403).body(Map.of("error", "Este curso no está disponible actualmente"));
            }

            List<Map<String, Object>> lecciones = db.jdbc().queryForList(
                    "SELECT LeccionID, CursoID, Orden, Titulo, Tipo, Contenido, CodigoEjemplo, XPLeccion, FechaCreacion, Preguntas " +
                            "FROM Lecciones WHERE CursoID = ? ORDER BY Orden", cursoId);

            for (Map<String, Object> lec : lecciones) {
                Object preguntas = lec.get("Preguntas");
                if (preguntas instanceof String s && !s.isBlank()) {
                    lec.put("Preguntas", objectMapper.readValue(s, Object.class));
                } else {
                    lec.put("Preguntas", null);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("curso", curso);
            response.put("lecciones", lecciones);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping
    @RequireAdmin
    public ResponseEntity<?> createCurso(@RequestBody Map<String, Object> body) {
        String titulo = (String) body.get("Titulo");
        Object instructorId = body.get("InstructorID");

        if (titulo == null || instructorId == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Titulo e InstructorID son requeridos"));
        }

        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            String descripcion = (String) body.get("Descripcion");
            String nivelDificultad = (String) body.getOrDefault("NivelDificultad", "principiante");
            String imagenPortada = (String) body.get("ImagenPortada");
            String idioma = (String) body.getOrDefault("Idioma", "Español");
            Boolean estaPublicado = body.get("EstaPublicado") != null ? (Boolean) body.get("EstaPublicado") : Boolean.FALSE;
            Boolean activo = body.get("Activo") != null ? (Boolean) body.get("Activo") : Boolean.FALSE;
            Integer xpRecompensa = body.get("XPRecompensa") != null ? ((Number) body.get("XPRecompensa")).intValue() : 100;

            Timestamp fechaCreacion = new Timestamp(System.currentTimeMillis());
            Timestamp fechaPublicacion = Boolean.TRUE.equals(estaPublicado) ? new Timestamp(System.currentTimeMillis()) : null;

            Integer cursoId = db.jdbc().queryForObject(
                    "INSERT INTO Cursos (Titulo, Descripcion, NivelDificultad, ImagenPortada, Idioma, EstaPublicado, Activo, FechaCreacion, FechaPublicacion, XPRecompensa, InstructorID) " +
                            "OUTPUT INSERTED.CursoID " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Integer.class,
                    titulo, descripcion, nivelDificultad, imagenPortada, idioma,
                    estaPublicado, activo, fechaCreacion, fechaPublicacion, xpRecompensa, instructorId
            );

            return ResponseEntity.status(201).body(Map.of("message", "Curso creado", "CursoID", cursoId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> updateCurso(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            String titulo = (String) body.get("Titulo");
            String descripcion = (String) body.get("Descripcion");
            String nivelDificultad = (String) body.get("NivelDificultad");
            String imagenPortada = (String) body.get("ImagenPortada");
            String idioma = (String) body.get("Idioma");
            Boolean estaPublicado = body.get("EstaPublicado") != null ? (Boolean) body.get("EstaPublicado") : Boolean.FALSE;
            Boolean activo = body.get("Activo") != null ? (Boolean) body.get("Activo") : Boolean.FALSE;
            Integer xpRecompensa = body.get("XPRecompensa") != null ? ((Number) body.get("XPRecompensa")).intValue() : null;
            Object instructorId = body.get("InstructorID");

            Timestamp fechaPublicacion = null;
            if (Boolean.TRUE.equals(estaPublicado)) {
                List<Map<String, Object>> actual = db.jdbc().queryForList(
                        "SELECT FechaPublicacion FROM Cursos WHERE CursoID = ?", id);
                Object actualFecha = actual.isEmpty() ? null : actual.get(0).get("FechaPublicacion");
                fechaPublicacion = actualFecha != null ? (Timestamp) actualFecha : new Timestamp(System.currentTimeMillis());
            }

            int rows = db.jdbc().update(
                    "UPDATE Cursos SET " +
                            "Titulo = COALESCE(?, Titulo), " +
                            "Descripcion = COALESCE(?, Descripcion), " +
                            "NivelDificultad = COALESCE(?, NivelDificultad), " +
                            "ImagenPortada = COALESCE(?, ImagenPortada), " +
                            "Idioma = COALESCE(?, Idioma), " +
                            "EstaPublicado = ?, " +
                            "Activo = ?, " +
                            "FechaPublicacion = CASE WHEN ? = 1 THEN ? ELSE FechaPublicacion END, " +
                            "XPRecompensa = COALESCE(?, XPRecompensa), " +
                            "InstructorID = COALESCE(?, InstructorID) " +
                            "WHERE CursoID = ?",
                    titulo, descripcion, nivelDificultad, imagenPortada, idioma,
                    estaPublicado, activo, estaPublicado, fechaPublicacion, xpRecompensa, instructorId, id
            );

            if (rows == 0) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));

            return ResponseEntity.ok(Map.of("message", "Curso actualizado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PatchMapping("/{id}/toggle-activo")
    @RequireAdmin
    public ResponseEntity<?> toggleActivo(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "UPDATE Cursos SET Activo = CASE WHEN Activo = 1 THEN 0 ELSE 1 END " +
                            "OUTPUT INSERTED.Activo WHERE CursoID = ?", id);

            if (rows.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));

            return ResponseEntity.ok(Map.of("message", "Estado actualizado", "Activo", rows.get(0).get("Activo")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PatchMapping("/{id}/toggle-publicado")
    @RequireAdmin
    public ResponseEntity<?> togglePublicado(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            List<Map<String, Object>> actual = db.jdbc().queryForList(
                    "SELECT EstaPublicado, FechaPublicacion FROM Cursos WHERE CursoID = ?", id);

            if (actual.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));

            boolean estaPublicado = Boolean.TRUE.equals(actual.get(0).get("EstaPublicado"));
            Object fechaPublicacionActual = actual.get(0).get("FechaPublicacion");
            Timestamp fechaPublicacion = !estaPublicado
                    ? (fechaPublicacionActual != null ? (Timestamp) fechaPublicacionActual : new Timestamp(System.currentTimeMillis()))
                    : (Timestamp) fechaPublicacionActual;

            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "UPDATE Cursos SET " +
                            "EstaPublicado = CASE WHEN EstaPublicado = 1 THEN 0 ELSE 1 END, " +
                            "FechaPublicacion = CASE WHEN EstaPublicado = 0 THEN ? ELSE FechaPublicacion END " +
                            "OUTPUT INSERTED.EstaPublicado WHERE CursoID = ?",
                    fechaPublicacion, id);

            return ResponseEntity.ok(Map.of("message", "Visibilidad actualizada", "EstaPublicado", rows.get(0).get("EstaPublicado")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> deleteCurso(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            int rows = db.jdbc().update("DELETE FROM Cursos WHERE CursoID = ?", id);
            if (rows == 0) return ResponseEntity.status(404).body(Map.of("error", "Curso no encontrado"));
            return ResponseEntity.ok(Map.of("message", "Curso eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }
}