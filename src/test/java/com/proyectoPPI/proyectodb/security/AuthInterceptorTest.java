package com.proyectoPPI.proyectodb.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link AuthInterceptor}. Se simula JwtUtil y las
 * peticiones/respuestas HTTP; se usa un controlador de prueba con metodos
 * anotados con @RequireAuth/@RequireAdmin para poder construir HandlerMethod
 * reales, tal como lo hace Spring MVC en tiempo de ejecucion.
 */
class AuthInterceptorTest {

    static class ControladorDePrueba {
        public void endpointPublico() {
        }

        @RequireAuth
        public void endpointRequiereLogin() {
        }

        @RequireAdmin
        public void endpointRequiereAdmin() {
        }
    }

    private JwtUtil jwtUtil;
    private AuthInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter cuerpoRespuesta;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = mock(JwtUtil.class);
        interceptor = new AuthInterceptor(jwtUtil);
        ReflectionTestUtils.setField(interceptor, "adminUsername", "admin");
        ReflectionTestUtils.setField(interceptor, "adminEmail", "admin@admin.com");

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        cuerpoRespuesta = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpoRespuesta));
    }

    private HandlerMethod handlerMethodPara(String nombreMetodo) throws NoSuchMethodException {
        Method metodo = ControladorDePrueba.class.getMethod(nombreMetodo);
        return new HandlerMethod(new ControladorDePrueba(), metodo);
    }

    @Test
    void preHandle_permiteContinuarSiElHandlerNoEsUnHandlerMethod() throws Exception {
        boolean resultado = interceptor.preHandle(request, response, new Object());

        assertThat(resultado).isTrue();
    }

    @Test
    void preHandle_permiteContinuarSiElEndpointNoRequiereAutenticacion() throws Exception {
        boolean resultado = interceptor.preHandle(request, response, handlerMethodPara("endpointPublico"));

        assertThat(resultado).isTrue();
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void preHandle_rechazaConCuandoNoVieneElHeaderAuthorization() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean resultado = interceptor.preHandle(request, response, handlerMethodPara("endpointRequiereLogin"));

        assertThat(resultado).isFalse();
        verify(response).setStatus(401);
        assertThat(cuerpoRespuesta.toString()).contains("Token de acceso requerido");
    }

    @Test
    void preHandle_rechazaConCuandoElHeaderNoTieneFormatoBearerValido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("TokenSinPrefijoBearer");

        boolean resultado = interceptor.preHandle(request, response, handlerMethodPara("endpointRequiereLogin"));

        assertThat(resultado).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void preHandle_rechazaConCuandoElTokenEsInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtUtil.validateToken("token-invalido")).thenThrow(mock(JwtException.class));

        boolean resultado = interceptor.preHandle(request, response, handlerMethodPara("endpointRequiereLogin"));

        assertThat(resultado).isFalse();
        verify(response).setStatus(403);
        assertThat(cuerpoRespuesta.toString()).contains("Token inválido");
    }

}
