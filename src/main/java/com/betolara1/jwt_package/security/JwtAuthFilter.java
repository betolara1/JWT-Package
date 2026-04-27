package com.betolara1.jwt_package.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.betolara1.jwt_package.config.JwtProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // O CONSTRUTOR RECEBE VIA INJEÇÃO DE DEPENDÊNCIA
    private final JwtUtil jwtUtil;
    private final JwtProperties properties;

    // O CONSTRUTOR RECEBE VIA INJEÇÃO DE DEPENDÊNCIA
    public JwtAuthFilter(JwtUtil jwtUtil, JwtProperties properties) {
        this.jwtUtil = jwtUtil;
        this.properties = properties;
    }

    // PARA PODER USAR WILDCARDS COMO /users/**
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); 

    // METODO CRIA OBRIGATORIO POR ESTENDER DE OncePerRequestFilter
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain)
                                    throws ServletException, IOException {

        
        String path = request.getRequestURI();

        String requestPath = path.substring(request.getContextPath().length());

        // METODO PARA PEGAR OS PATHS QUE SERAO EXCLUIDOS DO FILTRO
        if(properties.getExcludedPaths() != null){
            boolean isExcluded = properties.getExcludedPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
            if(isExcluded || "OPTIONS".equalsIgnoreCase(request.getMethod())){
                log.debug("Caminho liberado pelo filtro: {}.", requestPath);
                filterChain.doFilter(request, response);
                return;
            }
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
                log.info("Usuário {} autenticado via JWT.", username);
                // Cria autenticação baseada apenas no Token (Stateless) sem buscar no banco
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, java.util.Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            else{ log.warn("Tentativa de acesso negado: Token invalido para o path {}.", requestPath);}
        }

        filterChain.doFilter(request, response);

    }
}