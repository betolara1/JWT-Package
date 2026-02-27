package com.betolara1.jwt_package.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // <--- Import Novo
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // 1. Definimos quem codifica a senha
    // O BCryptPasswordEncoder é uma implementação do PasswordEncoder que usa o algoritmo BCrypt para codificar as senhas, o que é recomendado para segurança
    // O método passwordEncoder é obrigatório para configurar o Spring Security, pois ele precisa de um PasswordEncoder para codificar as senhas dos usuários
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Pegamos o AuthenticationManager pronto da configuração do Spring
    // O AuthenticationManager é o responsável por autenticar as credenciais do usuário
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}