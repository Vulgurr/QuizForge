package com.quizforge.backend.gestor;

import com.quizforge.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class GestorSeguridad {

    private static final String CLAIM_ROL = "rol";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final long expirationMs;

    public GestorSeguridad(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROL, usuario.getRol())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(signingKey)
                .compact();
    }

    public int extraerUsuarioId(String token) {
        Claims claims = parsearToken(token);
        return Integer.parseInt(claims.getSubject());
    }

    public String extraerRol(String token) {
        Claims claims = parsearToken(token);
        String rol = claims.get(CLAIM_ROL, String.class);
        if (rol == null || rol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido: rol ausente");
        }
        return rol;
    }

    public void validarRolAdmin(String rol) {
        if (!"ADMIN".equalsIgnoreCase(rol)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Se requiere rol ADMIN para realizar esta operación"
            );
        }
    }

    public boolean esPropietarioOAdmin(int usuarioId, int creadorId, String rol) {
        return "ADMIN".equalsIgnoreCase(rol) || usuarioId == creadorId;
    }

    private Claims parsearToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(resolverJwt(token))
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }

    private String resolverJwt(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token requerido");
        }
        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length()).trim();
        }
        return token.trim();
    }
}
