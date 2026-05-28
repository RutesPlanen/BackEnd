package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginTokenService loginTokenService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, loginTokenService);
    }

    private User nyBruger(String email, String password) {
        User user = new User();
        user.setName("Test Bruger");
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.CHAUFFEUR);
        return user;
    }

    // ── createUser ─────────────────────────────────────────────────────────────

    @Test
    void createUser_hashesPassword() {
        User input = nyBruger("ny@test.dk", "klartekst");
        when(userRepository.findFirstByEmail("ny@test.dk")).thenReturn(Optional.empty());
        when(loginTokenService.getPasswordEncoder())
                .thenReturn(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User gemt = userService.createUser(input);

        // Password må ikke være gemt som klartekst
        assertThat(gemt.getPassword()).isNotEqualTo("klartekst");
        assertThat(gemt.getPassword()).startsWith("$2a$"); // BCrypt-format
    }

    @Test
    void createUser_saetterActiveTilTrue() {
        User input = nyBruger("ny@test.dk", "password");
        when(userRepository.findFirstByEmail(any())).thenReturn(Optional.empty());
        when(loginTokenService.getPasswordEncoder())
                .thenReturn(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User gemt = userService.createUser(input);

        assertThat(gemt.getActive()).isTrue();
    }

    @Test
    void createUser_tomPassword_kasterBadRequest() {
        User input = nyBruger("ny@test.dk", "");

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createUser_nullPassword_kasterBadRequest() {
        User input = nyBruger("ny@test.dk", null);

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createUser_duplikatEmail_kasterConflict() {
        User input = nyBruger("eksisterende@test.dk", "password");
        when(userRepository.findFirstByEmail("eksisterende@test.dk")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    // ── updateUser ──────────────────────────────────────────────────────────────

    @Test
    void updateUser_opdatererKunIkkekNullFelter() {
        User eksisterende = nyBruger("gammel@test.dk", "gammelHash");
        eksisterende.setId(1L);
        eksisterende.setName("Gammelt Navn");
        when(userRepository.findById(1L)).thenReturn(Optional.of(eksisterende));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User opdatering = new User();
        opdatering.setName("Nyt Navn");
        // email og password er null – må ikke overskrives

        User result = userService.updateUser(1L, opdatering);

        assertThat(result.getName()).isEqualTo("Nyt Navn");
        assertThat(result.getEmail()).isEqualTo("gammel@test.dk");
        assertThat(result.getPassword()).isEqualTo("gammelHash");
    }

    @Test
    void updateUser_nytPassword_hashesDetNye() {
        var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        User eksisterende = nyBruger("test@test.dk", encoder.encode("gammelt"));
        eksisterende.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(eksisterende));
        when(loginTokenService.getPasswordEncoder()).thenReturn(encoder);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User opdatering = new User();
        opdatering.setPassword("nyt");

        User result = userService.updateUser(1L, opdatering);

        assertThat(encoder.matches("nyt", result.getPassword())).isTrue();
    }

    @Test
    void updateUser_brugerIkkeFound_kasterNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── deactivateUser ──────────────────────────────────────────────────────────

    @Test
    void deactivateUser_saetterActiveFalse() {
        User user = nyBruger("test@test.dk", "hash");
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.deactivateUser(1L);

        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deactivateUser_ikkeFound_kasterNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── deleteUser ──────────────────────────────────────────────────────────────

    @Test
    void deleteUser_kasterNotFound_naarBrugerMangles() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(42L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteUser_sletterBruger() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    // ── findAllUsers ────────────────────────────────────────────────────────────

    @Test
    void findAllUsers_returnerAlleFragRepository() {
        User u1 = nyBruger("a@test.dk", "h");
        User u2 = nyBruger("b@test.dk", "h");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<User> result = userService.findAllUsers();

        assertThat(result).containsExactly(u1, u2);
    }
}
