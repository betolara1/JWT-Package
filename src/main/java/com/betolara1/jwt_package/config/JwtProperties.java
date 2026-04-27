package com.betolara1.jwt_package.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ConfigurationProperties(prefix = "jwt") // O prefixo que será usado no application.properties
@Validated // ativa a validação
public class JwtProperties {

    // Chave secreta para a assinatura do token
    @NotBlank
    @Size(min = 32, message = "A chave secreta deve ter no mínimo 32 caracteres para segurança HS256")
    private String secretKey;

    // Tempo de expiração em ms
    private long expirationTime = 86400000;

    // Lista de caminhos ignorados pelo filtro de segurança
    private List<String> excludedPaths = new ArrayList<>();

    //Ativa ou desativa o filtro de segurança JWT.
    private boolean filterEnabled = true;


    public String getSecretKey(){ return secretKey;}
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getExpirationTime() { return expirationTime; }
    public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime; }

    public List<String> getExcludedPaths() { return excludedPaths; }
    public void setExcludedPaths(List<String> excludedPaths) { this.excludedPaths = excludedPaths; }

    public boolean isFilterEnabled() { return filterEnabled; }
    public void setFilterEnabled(boolean filterEnabled) { this.filterEnabled = filterEnabled; }
}
