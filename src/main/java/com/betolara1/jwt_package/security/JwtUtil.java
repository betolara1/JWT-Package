package com.betolara1.jwt_package.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;

import com.betolara1.jwt_package.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtUtil {
    private final JwtProperties properties;
    private final Key signingKey; // Declarada como final pra cachear

    public JwtUtil(JwtProperties properties){
        this.properties = properties;

        // O processamento pesado acontece aqui apenas UMA vez, quando o Spring sobe
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecretKey().getBytes());
    }

    private Key getSigningKey() {
        return this.signingKey; // Aqui ela só retorna o que já está pronto
    }

    
    // Chamar .setClaims() depois de .setSubject(), ele apaga o subject. Por isso, sempre passamos o Map de claims primeiro.
    public String generateToken(String username, Map<String, Object> extraClaims){
        return Jwts.builder()
                    .setClaims(extraClaims) // 1. Passa os dados extras
                    .setSubject(username)   // 2. Define o "dono" do token
                    .setIssuedAt(new Date(System.currentTimeMillis())) // Data de criação do token
                    .setExpiration(new Date(System.currentTimeMillis() + properties.getExpirationTime())) // Expiração do token 
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Assinatura do token
                    .compact();
    }


    // ESSE METODO VAI GERAR O TOKEN PRONTO 
    public String generateToken(String username){
        return generateToken(username, new HashMap<>());
    }


    // O <T> significa: "Eu vou retornar um tipo que você escolher na hora"
    // O ASSISTENTE (Poderoso e genérico)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        
        // 1. Abre a "mala" (Claims) e pega tudo o que tem lá dentro
        final Claims claims = extractAllClaims(token);
        
        // 2. Executa a "função" que você me passou para extrair o que você quer
        return claimsResolver.apply(claims);
    }



    // METODO PARA VALIDAR O TOKEN
    // O CHAVEIRO (Privado e direto)
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // METODO PARA EXTRAIR O USUARIO
    // OS ESPECIALISTAS (Atalhos simples) 
    public String extractUsername(String token){
        try{
            return extractClaim(token, Claims::getSubject);
        }
        catch(Exception e){
            log.warn("Token invalido ou expirado: {}", e.getMessage());
            return null;
        }
    }


    // O validateToken agora apenas tenta abrir o token. 
    // Se conseguir abrir sem dar erro, ele é válido!
    public boolean validateToken(String token){
        try{
            extractAllClaims(token); // Se isso aqui não der erro, o token é válido
            return true;
        }
        catch(JwtException | IllegalArgumentException e){
            log.warn("Token invalido ou expirado: {}", e.getMessage());
            return false;
        }
    }
}