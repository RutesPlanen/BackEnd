package dk.rutesplanen.backend.repositories;

import dk.rutesplanen.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository giver automatisk findAll, findById, save, delete m.m. uden at skrive SQL
public interface UserRepository extends JpaRepository<User, Long> {

    // Bruges ved login – Spring Data genererer SQL automatisk ud fra metodenavnet
    Optional<User> findByEmailAndPassword(String email, String password);

    // Bruges til at tjekke om en email allerede er i brug
    Optional<User> findFirstByEmail(String email);
}