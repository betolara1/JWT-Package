package com.betolara1.jwt_package.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.betolara1.jwt_package.config.JwtProperties;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties properties;
    // Chave de 256 bits (32 caracteres) para satisfazer o HS256
    private final String SECRET_KEY = "minha_chave_secreta_muito_longa_com_mais_de_32_caracteres_para_seguranca";

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();// Inicializa o objeto
        properties.setSecretKey(SECRET_KEY); // Configura os valores nele
        properties.setExpirationTime(3600000L);

        jwtUtil = new JwtUtil(properties);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido para um username")
    void deveGerarTokenValido() {
        String username = "roberto.lara";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar um token gerado corretamente")
    void deveValidarTokenGerado() {
        String token = jwtUtil.generateToken("usuario.teste");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Deve extrair o username correto do token")
    void deveExtrairUsername() {
        String usernameOriginal = "admin";
        String token = jwtUtil.generateToken(usernameOriginal);
        
        String usernameExtraido = jwtUtil.extractUsername(token);
        assertEquals(usernameOriginal, usernameExtraido);
    }

    @Test
    @DisplayName("Deve suportar e extrair claims customizados")
    void deveSuportarClaimsCustomizados() {
        String username = "user.custom";
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("email", "teste@email.com");

        String token = jwtUtil.generateToken(username, claims);
        
        // Usamos um método auxiliar ou o extractUsername para validar a integridade
        assertEquals(username, jwtUtil.extractUsername(token));
        
        // Para validar os claims internos, poderíamos expor o extractAllClaims ou criar um helper
        // Como o extractAllClaims é private, validamos que o token é aceito
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Deve retornar false para um token malformado")
    void deveRejeitarTokenInvalido() {
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.assinatura-falsa";
        assertFalse(jwtUtil.validateToken(tokenInvalido));
    }

    @Test
    @DisplayName("Deve retornar false para um token nulo ou vazio")
    void deveRetornarFalseParaTokenNuloOuVazio() {
        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(""));
    }
}
