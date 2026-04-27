package com.betolara1.jwt_package.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.betolara1.jwt_package.security.JwtAuthFilter;
import com.betolara1.jwt_package.security.JwtUtil;

@AutoConfiguration
@ConditionalOnClass(io.jsonwebtoken.Jwts.class) // Só ativa se a lib JJWT existir no projeto
@EnableConfigurationProperties(JwtProperties.class) // Ativa a leitura das propriedades
public class JwtAutoConfiguration {

    /**
     * Define o bean JwtUtil.
     * @ConditionalOnMissingBean é fundamental: se o desenvolvedor que usa sua lib
     * criar o próprio bean JwtUtil, o Spring usará o dele e ignorará este aqui.
     */
    // Agora o Spring injeta o objeto JwtProperties automaticamente aqui!
    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(JwtProperties properties){
        return new JwtUtil(properties);
    }


    /**
     * Define o filtro de segurança.
     * Spring injeta automaticamente o JwtUtil criado no método acima.
     */
    @Bean
    @ConditionalOnMissingBean
    // Só cria o filtro se 'jwt.security.enabled' for 'true'. 
    // Se não existir a propriedade, o padrão é 'true' (matchIfMissing).
    @ConditionalOnProperty(name = "jwt.filter.enabled", havingValue = "true", matchIfMissing = true)
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, JwtProperties properties){
        return new JwtAuthFilter(jwtUtil, properties);
    }

}
