package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginTokenService {

    // Tokens gemmes i hukommelsen – de forsvinder ved genstart af serveren
    private final Map<String, User> aktiveSessioner = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public LoginTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Map.Entry<String, User>> login(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password)
                // Afvis login hvis brugeren er deaktiveret (active = false)
                .filter(u -> !Boolean.FALSE.equals(u.getActive()))
                .map(user -> {
                    // Generer et unikt tilfældigt token og gem det med brugeren
                    String token = UUID.randomUUID().toString();
                    aktiveSessioner.put(token, user);
                    return Map.entry(token, user);
                });
    }

    // Slår token op i sessionskortet og returnerer den tilhørende bruger hvis det er gyldigt
    public Optional<User> validerToken(String token) {
        return Optional.ofNullable(aktiveSessioner.get(token));
    }
}
