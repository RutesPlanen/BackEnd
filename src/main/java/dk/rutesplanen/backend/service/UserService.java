package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LoginTokenService loginTokenService;

    public UserService(UserRepository userRepository, LoginTokenService loginTokenService) {
        this.userRepository = userRepository;
        this.loginTokenService = loginTokenService;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adgangskode må ikke være tom");
        }
        if (userRepository.findFirstByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email er allerede i brug");
        }
        user.setPassword(loginTokenService.getPasswordEncoder().encode(user.getPassword()));
        user.setActive(true);
        return userRepository.save(user);
    }

    public User updateUser(Long id, User opdateret) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruger ikke fundet"));
        if (opdateret.getName() != null) user.setName(opdateret.getName());
        if (opdateret.getEmail() != null) user.setEmail(opdateret.getEmail());
        if (opdateret.getPassword() != null && !opdateret.getPassword().isBlank()) {
            user.setPassword(loginTokenService.getPasswordEncoder().encode(opdateret.getPassword()));
        }
        if (opdateret.getRole() != null) user.setRole(opdateret.getRole());
        if (opdateret.getRestaurant() != null) user.setRestaurant(opdateret.getRestaurant());
        if (opdateret.getActive() != null) user.setActive(opdateret.getActive());
        return userRepository.save(user);
    }

    // Sætter active=false i stedet for at slette – historik og relationer bevares
    public User deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruger ikke fundet"));
        user.setActive(false);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruger ikke fundet");
        }
        userRepository.deleteById(id);
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
}