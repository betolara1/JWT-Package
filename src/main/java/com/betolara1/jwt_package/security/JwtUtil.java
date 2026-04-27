package com.betolara1.jwt_package.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    // CHAVE SECRETA PARA ASSINAR O TOKEN
    @Value("${secret.key}")
    private String key;

    // TEMPO DE EXPIRAÇÃO DO TOKEN (Lido do properties ou 24h por padrão)
    @Value("${jwt.expiration-time:86400000}")
    private long expirationTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    
    // Chamar .setClaims() depois de .setSubject(), ele apaga o subject. Por isso, sempre passamos o Map de claims primeiro.
    public String generateToken(String username, Map<String, Object> extraClaims){
        return Jwts.builder()
                    .setClaims(extraClaims) // 1. Passa os dados extras
                    .setSubject(username)   // 2. Define o "dono" do token
                    .setIssuedAt(new Date(System.currentTimeMillis())) // Data de criação do token
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Expiração do token 
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Assinatura do token
                    .compact();
    }


    // ESSE METODO VAI GERAR O TOKEN PRONTO 
    public String generateToken(String username){
        return generateToken(username, new HashMap<>());
    }


    // METODO PARA VALIDAR O TOKEN
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // METODO PARA EXTRAIR O USUARIO
    public String extractUsername(String token){
        try{
            return extractAllClaims(token).getSubject();
        }
        catch(Exception e){
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
            return false;
        }
    }
}