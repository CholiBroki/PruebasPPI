package com.proyectoPPI.proyectodb.controllers;

import com.proyectoPPI.proyectodb.db.Db;
import com.proyectoPPI.proyectodb.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Db db;
    private final JwtUtil jwtUtil;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    public AuthController(Db db, JwtUtil jwtUtil) {
        this.db = db;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        String nombreUsuario = asString(body.get("NombreUsuario"));
        String contrasena = asString(body.get("Contrasena"));

        if (nombreUsuario == null || contrasena == null) {
            return ResponseEntity.status(400).body(Map.of("error", "NombreUsuario y Contrasena son requeridos"));
        }

        try {
            if (!db.isAvailable()) {
                return ResponseEntity.status(503).body(Map.of("error", "Base de datos no disponible. Intente más tarde."));
            }

            boolean includeRole = db.hasColumn("Usuarios", "Rol");
            String selectColumns = includeRole
                    ? "UsuarioID, NombreUsuario, CorreoElectronico, PasswordHash, PasswordSalt, Activo, EmailVerificado, Rol"
                    : "UsuarioID, NombreUsuario, CorreoElectronico, PasswordHash, PasswordSalt, Activo, EmailVerificado";

            List<Map<String, Object>> rows = db.jdbc().queryForList(
                    "SELECT " + selectColumns + " FROM Usuarios WHERE NombreUsuario = ? OR CorreoElectronico = ?",
                    nombreUsuario, nombreUsuario
            );

            if (rows.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña inválidos"));
            }

            Map<String, Object> user = rows.get(0);

            boolean activo = Boolean.TRUE.equals(user.get("Activo"));
            if (!activo) {
                return ResponseEntity.status(403).body(Map.of("error", "Usuario inactivo"));
            }

            String passwordHash = (String) user.get("PasswordHash");
            String passwordSalt = (String) user.get("PasswordSalt");

            boolean passwordMatch;
            if (isBcryptHash(passwordHash)) {
                passwordMatch = BCrypt.checkpw(contrasena, passwordHash);
            } else {
                String hashFromSalt = sha512((passwordSalt == null ? "" : passwordSalt) + contrasena);
                String hashFromSaltAlt = sha512(contrasena + (passwordSalt == null ? "" : passwordSalt));
                passwordMatch = hashFromSalt.equals(passwordHash) || hashFromSaltAlt.equals(passwordHash);
            }

            if (!passwordMatch) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña inválidos"));
            }

            String rol = (String) user.get("Rol");
            if (rol == null) {
                rol = isAdminUser(user) ? "admin" : "user";
            }

            Integer usuarioId = (Integer) user.get("UsuarioID");
            String nombreUsuarioResp = (String) user.get("NombreUsuario");
            String correo = (String) user.get("CorreoElectronico");

            String token = jwtUtil.generateToken(usuarioId, nombreUsuarioResp, correo, rol);

            Map<String, Object> usuario = new HashMap<>();
            usuario.put("UsuarioID", usuarioId);
            usuario.put("NombreUsuario", nombreUsuarioResp);
            usuario.put("CorreoElectronico", correo);
            usuario.put("Activo", activo);
            usuario.put("EmailVerificado", user.get("EmailVerificado"));
            usuario.put("Rol", rol);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login correcto");
            response.put("token", token);
            response.put("usuario", usuario);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    private boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    private boolean isAdminUser(Map<String, Object> user) {
        return adminUsername.equals(user.get("NombreUsuario")) || adminEmail.equals(user.get("CorreoElectronico"));
    }

    private String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
