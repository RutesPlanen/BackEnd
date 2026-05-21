package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginTokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    private record SessionEntry(User user, Instant expiresAt) {}

    private final Map<String, SessionEntry> aktiveSessioner = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<Map.Entry<String, User>> login(String email, String password) {
        return userRepository.findByEmail(email)
                // Afvis login hvis brugeren er deaktiveret
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                // Valider adgangskode mod BCrypt-hash
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    aktiveSessioner.put(token, new SessionEntry(user, Instant.now().plus(TOKEN_TTL)));
                    return Map.entry(token, user);
                });
    }

    public Optional<User> validerToken(String token) {
        SessionEntry entry = aktiveSessioner.get(token);
        if (entry == null) return Optional.empty();
        if (Instant.now().isAfter(entry.expiresAt())) {
            aktiveSessioner.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.user());
    }

    public void logout(String token) {
        aktiveSessioner.remove(token);
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}