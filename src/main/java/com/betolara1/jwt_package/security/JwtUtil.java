package com.betolara1.jwt_package.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
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

    // ESSE METODO VAI GERAR O TOKEN PRONTO 
    public String generateToken(String username){
        return Jwts.builder()
                    .setSubject(username)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                    .compact();
    }


    // METODO PARA EXTRAIR O USUARIO
    public String extractUsername(String token){
        try{
            return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
        }
        catch(JwtException | IllegalArgumentException e){
            return null;
        }
    }


    // METODO PARA VALIDAR O TOKEN
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }
        catch(JwtException | IllegalArgumentException e){
            return false;
        }
    }
}