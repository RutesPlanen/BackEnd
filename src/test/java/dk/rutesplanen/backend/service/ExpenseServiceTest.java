package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Expense;
import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(expenseRepository);
    }

    private User chauffeur() {
        User u = new User();
        u.setId(1L);
        u.setRole(Role.CHAUFFEUR);
        u.setActive(true);
        return u;
    }

    private Expense nyUdgift(double beloeb) {
        Expense e = new Expense();
        e.setAmount(beloeb);
        e.setCategory("Benzin");
        e.setChauffeur(chauffeur());
        return e;
    }

    // ── createExpense ───────────────────────────────────────────────────────────

    @Test
    void createExpense_gemmerOgReturnerer() {
        Expense udgift = nyUdgift(150.0);
        when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Expense result = expenseService.createExpense(udgift);

        assertThat(result.getAmount()).isEqualTo(150.0);
        verify(expenseRepository).save(udgift);
    }

    // ── findById ────────────────────────────────────────────────────────────────

    @Test
    void findById_returnererUdgift() {
        Expense udgift = nyUdgift(200.0);
        udgift.setId(1L) ;
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(udgift));

        Optional<Expense> result = expenseService.findById(1L);

        assertThat(result).isPresent().contains(udgift);
    }

    @Test
    void findById_ikkeFound_returnerTom() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Expense> result = expenseService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ── findByChauffeur ─────────────────────────────────────────────────────────

    @Test
    void findByChauffeur_returnerKunEgneUdgifter() {
        User chf = chauffeur();
        Expense e1 = nyUdgift(100.0);
        Expense e2 = nyUdgift(200.0);
        when(expenseRepository.findByChauffeur(chf)).thenReturn(List.of(e1, e2));

        List<Expense> result = expenseService.findByChauffeur(chf);

        assertThat(result).containsExactly(e1, e2);
    }

    // ── deleteExpense ───────────────────────────────────────────────────────────

    @Test
    void deleteExpense_sletterHvisFound() {
        when(expenseRepository.existsById(1L)).thenReturn(true);

        expenseService.deleteExpense(1L);

        verify(expenseRepository).deleteById(1L);
    }

    @Test
    void deleteExpense_ikkeFound_kasterNotFound() {
        when(expenseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.deleteExpense(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(expenseRepository, never()).deleteById(any());
    }

    // ── findAll ─────────────────────────────────────────────────────────────────

    @Test
    void findAll_returnerAlle() {
        Expense e1 = nyUdgift(100.0);
        Expense e2 = nyUdgift(300.0);
        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Expense> result = expenseService.findAll();

        assertThat(result).hasSize(2).containsExactly(e1, e2);
    }
}
