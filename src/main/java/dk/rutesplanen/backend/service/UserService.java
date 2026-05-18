package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service til håndtering af brugere.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // Nye brugere sættes altid til aktive ved oprettelse
        user.setActive(true);
        return userRepository.save(user);
    }

    public User updateUser(Long id, User opdateret) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bruger ikke fundet"));
        // Opdater kun de felter der faktisk sendes – null-felter rører vi ikke
        if (opdateret.getName() != null) user.setName(opdateret.getName());
        if (opdateret.getEmail() != null) user.setEmail(opdateret.getEmail());
        // Adgangskode opdateres kun hvis der sendes en ikke-tom ny adgangskode
        if (opdateret.getPassword() != null && !opdateret.getPassword().isBlank()) {
            user.setPassword(opdateret.getPassword());
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