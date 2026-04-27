package com.betolara1.jwt_package.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.betolara1.jwt_package.config.JwtProperties;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private JwtUtil jwtUtil;
    private JwtProperties properties;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtil = Mockito.mock(JwtUtil.class);
        properties = new JwtProperties(); // Inicializamos as propriedades
        jwtAuthFilter = new JwtAuthFilter(jwtUtil, properties);
        filterChain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve ignorar autenticação para caminhos excluídos no properties")
    void deveIgnorarCaminhosExcluidos() throws ServletException, IOException {
        // Configuramos os caminhos excluídos diretamente no objeto de propriedades
        properties.setExcludedPaths(List.of("/public/**", "/swagger-ui/**"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/login");
        request.setContextPath("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verifica se o filtro apenas passou a requisição adiante sem validar token
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve autenticar quando um token válido é fornecido")
    void deveAutenticarComTokenValido() throws ServletException, IOException {
        properties.setExcludedPaths(List.of());
        
        String token = "token.valido.aqui";
        String username = "roberto";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/resource");
        request.setContextPath("");
        request.addHeader("Authorization", "Bearer " + token);
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Deve liberar requisições OPTIONS automaticamente (CORS)")
    void deveLiberarOptions() throws ServletException, IOException {
        properties.setExcludedPaths(List.of());
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("OPTIONS");
        request.setRequestURI("/api/resource");
        request.setContextPath("");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }
}
