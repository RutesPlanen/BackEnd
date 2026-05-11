package dk.rutesplanen.backend.config;

import dk.rutesplanen.backend.service.LoginTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    private final LoginTokenService loginTokenService;

    public BearerTokenFilter(LoginTokenService loginTokenService) {
        this.loginTokenService = loginTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Login-endpointet kræver ikke token – det er her tokenet udstedes
        if (path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Statiske filer (HTML/CSS/JS) kræver ikke token
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Tjek at Authorization-headeren er til stede og starter med "Bearer "
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Valider tokenet mod sessionskortet – afvis hvis det er ukendt
        String token = header.substring(7);
        if (loginTokenService.validerToken(token).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}