package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    private LoginTokenService loginTokenService;

    @BeforeEach
    void setUp() {
        loginTokenService = new LoginTokenService(userRepository);
    }

    private User aktivBruger(String email, String klartekstPassword) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(loginTokenService.getPasswordEncoder().encode(klartekstPassword));
        user.setRole(Role.CHAUFFEUR);
        user.setActive(true);
        return user;
    }

    @Test
    void login_medGyldigeCredentials_returnerToken() {
        User user = aktivBruger("test@test.dk", "hemmeligt");
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));

        Optional<Map.Entry<String, User>> result = loginTokenService.login("test@test.dk", "hemmeligt");

        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isNotBlank();
        assertThat(result.get().getValue()).isEqualTo(user);
    }

    @Test
    void login_medForkertPassword_returnerTom() {
        User user = aktivBruger("test@test.dk", "rigtigtPassword");
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));

        Optional<Map.Entry<String, User>> result = loginTokenService.login("test@test.dk", "forkertPassword");

        assertThat(result).isEmpty();
    }

    @Test
    void login_brugerEksistererIkke_returnerTom() {
        when(userRepository.findFirstByEmail("ukendt@test.dk")).thenReturn(Optional.empty());

        Optional<Map.Entry<String, User>> result = loginTokenService.login("ukendt@test.dk", "password");

        assertThat(result).isEmpty();
    }

    @Test
    void login_deaktiveretBruger_returnerTom() {
        User user = aktivBruger("test@test.dk", "hemmeligt");
        user.setActive(false);
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));

        Optional<Map.Entry<String, User>> result = loginTokenService.login("test@test.dk", "hemmeligt");

        assertThat(result).isEmpty();
    }

    @Test
    void login_brugerMedNullActive_returnerTom() {
        // Regressions-test: active=null må ikke behandles som aktiv
        User user = aktivBruger("test@test.dk", "hemmeligt");
        user.setActive(null);
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));

        Optional<Map.Entry<String, User>> result = loginTokenService.login("test@test.dk", "hemmeligt");

        assertThat(result).isEmpty();
    }

    @Test
    void validerToken_gyldigtToken_returnerBruger() {
        User user = aktivBruger("test@test.dk", "hemmeligt");
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));
        String token = loginTokenService.login("test@test.dk", "hemmeligt").get().getKey();

        Optional<User> result = loginTokenService.validerToken(token);

        assertThat(result).isPresent().contains(user);
    }

    @Test
    void validerToken_ugyldtToken_returnerTom() {
        Optional<User> result = loginTokenService.validerToken("ikke-et-rigtigt-token");

        assertThat(result).isEmpty();
    }

    @Test
    void logout_fjenerToken() {
        User user = aktivBruger("test@test.dk", "hemmeligt");
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));
        String token = loginTokenService.login("test@test.dk", "hemmeligt").get().getKey();

        loginTokenService.logout(token);

        assertThat(loginTokenService.validerToken(token)).isEmpty();
    }

    @Test
    void login_toLogins_giver_toForkelligeTokens() {
        User user = aktivBruger("test@test.dk", "hemmeligt");
        when(userRepository.findFirstByEmail("test@test.dk")).thenReturn(Optional.of(user));

        String token1 = loginTokenService.login("test@test.dk", "hemmeligt").get().getKey();
        String token2 = loginTokenService.login("test@test.dk", "hemmeligt").get().getKey();

        assertThat(token1).isNotEqualTo(token2);
    }
}
