package com.quizforge.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Memoria caché para guardar los buckets por dirección IP
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = obtenerIpCliente(request);
        Bucket bucket = cache.computeIfAbsent(ip, this::crearNuevoBucket);

        // Intenta consumir 1 token
        if (bucket.tryConsume(1)) {
            return true; // Pasa la petición al Controller
        }

        // Si no hay tokens, bloquea y devuelve 429
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Demasiadas peticiones. Por favor, intenta mas tarde.");
        return false;
    }

    private Bucket crearNuevoBucket(String ip) {
        // Configuración: 5 peticiones máximas, recarga 5 tokens cada 1 minuto
        Refill recarga = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limite = Bandwidth.classic(5, recarga);

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // Si estás detrás de un proxy/Nginx
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}