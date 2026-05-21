package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Expense;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.ExpenseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public List<Expense> findByChauffeur(User chauffeur) {
        return expenseRepository.findByChauffeur(chauffeur);
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Udgift ikke fundet");
        }
        expenseRepository.deleteById(id);
    }
}