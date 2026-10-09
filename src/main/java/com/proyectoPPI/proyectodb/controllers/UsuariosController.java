package com.proyectoPPI.proyectodb.controllers;

import com.proyectoPPI.proyectodb.db.Db;
import com.proyectoPPI.proyectodb.security.RequireAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    private final Db db;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${admin.nombre}")
    private String adminNombre;
    @Value("${admin.apellido}")
    private String adminApellido;

    public UsuariosController(Db db) {
        this.db = db;
    }

    private String selectColumns() {
        boolean includeRole = db.hasColumn("Usuarios", "Rol");
        return includeRole
                ? "UsuarioID, Nombre, Apellido, NombreUsuario, CorreoElectronico, Activo, EmailVerificado, Rol"
                : "UsuarioID, Nombre, Apellido, NombreUsuario, CorreoElectronico, Activo, EmailVerificado";
    }

    @GetMapping
    @RequireAdmin
    public ResponseEntity<?> getUsuarios() {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList("SELECT " + selectColumns() + " FROM Usuarios");
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> getUsuarioById(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + selectColumns() + " FROM Usuarios WHERE UsuarioID = ?", id);
            if (rows.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            return ResponseEntity.ok(rows.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Map<String, Object> body) {
        String nombreUsuario = (String) body.get("NombreUsuario");
        String correoElectronico = (String) body.get("CorreoElectronico");
        String contrasena = (String) body.get("Contrasena");
        String nombre = (String) body.getOrDefault("Nombre", "");
        String apellido = (String) body.getOrDefault("Apellido", "");

        if (nombreUsuario == null || correoElectronico == null || contrasena == null) {
            return ResponseEntity.status(400).body(Map.of("error", "NombreUsuario, CorreoElectronico y Contrasena son requeridos"));
        }

        try {
            String salt = BCrypt.gensalt(10);
            String hash = BCrypt.hashpw(contrasena, salt);

            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            boolean hasRoleColumn = db.hasColumn("Usuarios", "Rol");

            Integer usuarioId;
            if (hasRoleColumn) {
                usuarioId = db.jdbc().queryForObject(
                        "INSERT INTO Usuarios (Nombre, Apellido, CorreoElectronico, NombreUsuario, PasswordHash, PasswordSalt, AceptaTerminos, Activo, EmailVerificado, Rol) " +
                                "OUTPUT INSERTED.UsuarioID VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Integer.class,
                        nombre, apellido, correoElectronico, nombreUsuario, hash, salt, false, true, false, "user"
                );
            } else {
                usuarioId = db.jdbc().queryForObject(
                        "INSERT INTO Usuarios (Nombre, Apellido, CorreoElectronico, NombreUsuario, PasswordHash, PasswordSalt, AceptaTerminos, Activo, EmailVerificado) " +
                                "OUTPUT INSERTED.UsuarioID VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Integer.class,
                        nombre, apellido, correoElectronico, nombreUsuario, hash, salt, false, true, false
                );
            }

            return ResponseEntity.status(201).body(Map.of("message", "Usuario creado", "UsuarioID", usuarioId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> updateUsuario(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));

            String nombre = (String) body.getOrDefault("Nombre", "");
            String apellido = (String) body.getOrDefault("Apellido", "");
            String nombreUsuario = (String) body.get("NombreUsuario");
            String correoElectronico = (String) body.get("CorreoElectronico");
            Boolean activo = body.get("Activo") != null ? (Boolean) body.get("Activo") : Boolean.TRUE;
            Boolean emailVerificado = body.get("EmailVerificado") != null ? (Boolean) body.get("EmailVerificado") : Boolean.FALSE;
            Object rol = body.get("Rol");

            boolean hasRoleColumn = db.hasColumn("Usuarios", "Rol");

            if (hasRoleColumn && rol != null) {
                db.jdbc().update(
                        "UPDATE Usuarios SET Nombre = ?, Apellido = ?, NombreUsuario = ?, CorreoElectronico = ?, Activo = ?, EmailVerificado = ?, Rol = ? WHERE UsuarioID = ?",
                        nombre, apellido, nombreUsuario, correoElectronico, activo, emailVerificado, rol, id
                );
            } else {
                db.jdbc().update(
                        "UPDATE Usuarios SET Nombre = ?, Apellido = ?, NombreUsuario = ?, CorreoElectronico = ?, Activo = ?, EmailVerificado = ? WHERE UsuarioID = ?",
                        nombre, apellido, nombreUsuario, correoElectronico, activo, emailVerificado, id
                );
            }

            return ResponseEntity.ok(Map.of("message", "Usuario actualizado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<?> deleteUsuario(@PathVariable int id) {
        try {
            if (!db.isAvailable()) return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible"));
            db.jdbc().update("DELETE FROM Usuarios WHERE UsuarioID = ?", id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    public void seedAdminUser() {
        try {
            if (!db.isAvailable()) {
                System.out.println("No se pudo inicializar la conexión a la base de datos. No se creó el usuario administrador.");
                return;
            }

            List<Map<String, Object>> existing = db.jdbc().queryForList(
                    "SELECT UsuarioID FROM Usuarios WHERE NombreUsuario = ? OR CorreoElectronico = ?",
                    adminUsername, adminEmail
            );

            if (!existing.isEmpty()) {
                return;
            }

            String passwordSalt = BCrypt.gensalt(10);
            String passwordHash = BCrypt.hashpw(adminPassword, passwordSalt);

            boolean includeRole = db.hasColumn("Usuarios", "Rol");

            if (includeRole) {
                db.jdbc().update(
                        "INSERT INTO Usuarios (Nombre, Apellido, CorreoElectronico, NombreUsuario, PasswordHash, PasswordSalt, AceptaTerminos, Activo, EmailVerificado, Rol) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        adminNombre, adminApellido, adminEmail, adminUsername, passwordHash, passwordSalt, false, true, true, "admin"
                );
            } else {
                db.jdbc().update(
                        "INSERT INTO Usuarios (Nombre, Apellido, CorreoElectronico, NombreUsuario, PasswordHash, PasswordSalt, AceptaTerminos, Activo, EmailVerificado) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        adminNombre, adminApellido, adminEmail, adminUsername, passwordHash, passwordSalt, false, true, true
                );
            }

            System.out.println("Usuario administrador creado: " + adminUsername);
        } catch (Exception error) {
            System.out.println("Error al crear usuario administrador: " + error.getMessage());
        }
    }
}
