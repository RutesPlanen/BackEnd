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

        // Tillad preflight OPTIONS requests uden token
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Login-endpointet kræver ikke token
        if (path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Map config kræver ikke token
        if (path.equals("/api/map/config")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Statiske filer kræver ikke token
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = header.substring(7);
        if (loginTokenService.validerToken(token).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }}