package com.proyectoPPI.proyectodb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean requiresAuth = handlerMethod.hasMethodAnnotation(RequireAuth.class)
                || handlerMethod.hasMethodAnnotation(RequireAdmin.class);
        if (!requiresAuth) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null) {
            String[] parts = authHeader.split(" ");
            if (parts.length == 2) {
                token = parts[1];
            }
        }

        if (token == null) {
            sendError(response, 401, "Token de acceso requerido");
            return false;
        }

        Claims claims;
        try {
            claims = jwtUtil.validateToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            sendError(response, 403, "Token inválido");
            return false;
        }

        request.setAttribute("currentUser", claims);

        if (handlerMethod.hasMethodAnnotation(RequireAdmin.class) && !isAdminUser(claims)) {
            sendError(response, 403, "Requiere permisos de administrador");
            return false;
        }

        return true;
    }

    private boolean isAdminUser(Claims claims) {
        String rol = claims.get("Rol", String.class);
        String nombreUsuario = claims.get("NombreUsuario", String.class);
        String correoElectronico = claims.get("CorreoElectronico", String.class);
        return "admin".equals(rol)
                || adminUsername.equals(nombreUsuario)
                || adminEmail.equals(correoElectronico);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }
}
