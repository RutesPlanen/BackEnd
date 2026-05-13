package dk.rutesplanen.backend.controller;

import dk.rutesplanen.backend.model.LoginRequest;
import dk.rutesplanen.backend.model.LoginResponse;
import dk.rutesplanen.backend.service.LoginTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginTokenService loginTokenService;

    public AuthController(LoginTokenService loginTokenService) {
        this.loginTokenService = loginTokenService;
    }

    // Modtager email+adgangskode, returnerer token+brugerinfo hvis det matcher – ellers 401
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return loginTokenService.login(request.getEmail(), request.getPassword())
                .map(entry -> ResponseEntity.ok(new LoginResponse(entry.getKey(), entry.getValue())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Frontend kalder dette for at tjekke om tokenet stadig er gyldigt (BearerTokenFilter validerer det)
    @GetMapping("/validate")
    public ResponseEntity<Void> validate() {
        return ResponseEntity.ok().build();
    }
}
