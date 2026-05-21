package dk.rutesplanen.backend.repositories;

import dk.rutesplanen.backend.model.Expense;
import dk.rutesplanen.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository giver automatisk findAll, findById, save, delete m.m. uden at skrive SQL
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Bruges til at hente chaufførens egne udgifter
    List<Expense> findByChauffeur(User chauffeur);
}