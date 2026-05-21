package dk.rutesplanen.backend.controller;

import dk.rutesplanen.backend.model.Expense;
import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.service.ExpenseService;
import dk.rutesplanen.backend.service.LoginTokenService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final LoginTokenService loginTokenService;

    public ExpenseController(ExpenseService expenseService, LoginTokenService loginTokenService) {
        this.expenseService = expenseService;
        this.loginTokenService = loginTokenService;
    }

    // Det er kun ADMIN der kan se alle udgifter
    @GetMapping
    public List<Expense> getAllExpenses(HttpServletRequest request) {
        requireAdmin(request);
        return expenseService.findAll();
    }

    // Returnerer kun den indloggede chaufførs egne udgifter
    @GetMapping("/mine")
    public List<Expense> getMyExpenses(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return expenseService.findByChauffeur(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(@RequestBody Expense expense, HttpServletRequest request) {
        User user = getCurrentUser(request);
        // Sæt chauffeur automatisk fra token. Brugeren kan ikke snyde og registrere for en anden
        expense.setChauffeur(user);
        return expenseService.createExpense(expense);
    }

    // Kun ADMIN eller udgiftens ejer kan slette
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id, HttpServletRequest request) {
        User user = getCurrentUser(request);
        Expense expense = expenseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Udgift ikke fundet"));
        boolean isOwner = expense.getChauffeur() != null &&
                expense.getChauffeur().getId().equals(user.getId());
        if (user.getRole() != Role.ADMIN && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du kan kun slette dine egne udgifter");
        }
        expenseService.deleteExpense(id);
    }

    private User getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return loginTokenService.validerToken(header.substring(7))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private void requireAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kun administratorer har adgang");
        }
    }
}