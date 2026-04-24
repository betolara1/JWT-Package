package com.betolara1.jwt_package.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // O CONSTRUTOR RECEBE APENAS JwtUtil VIA INJEÇÃO DE DEPENDÊNCIA
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // METODO CRIA OBRIGATORIO POR ESTENDER DE OncePerRequestFilter
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // IGNORAR AS REQUISIÇÕES DE DOCUMENTAÇÃO E ATUATOR PARA NÃO PRECISAR DE
        // AUTENTICAÇÃO NELAS
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)
                || path.startsWith(request.getContextPath() + "/v3/api-docs")
                || path.startsWith(request.getContextPath() + "/swagger")
                || path.startsWith(request.getContextPath() + "/webjars")
                || path.startsWith(request.getContextPath() + "/swagger-ui")
                || path.equals(request.getContextPath() + "/swagger-ui.html")
                || path.startsWith(request.getContextPath() + "/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // EXTRAIR O TOKEN DO HEADER Authorization
        // CONDIÇÃO PARA SABER SE O USUARIO FOI AUTORIZADO
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        // SE CHEGOU AQUI, SIGNIFICA QUE O USUÁRIO TEM UM TOKEN, ENTÃO VAMOS EXTRAIR O
        // USUÁRIO DO TOKEN E VALIDAR O TOKEN
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token)) {
                // Cria autenticação baseada apenas no Token (Stateless) sem buscar no banco
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, java.util.Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}