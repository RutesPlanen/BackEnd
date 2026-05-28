package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Restaurant;
import dk.rutesplanen.backend.repositories.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(restaurantRepository);
    }

    private Restaurant nyRestaurant(String navn) {
        Restaurant r = new Restaurant();
        r.setId(1L);
        r.setName(navn);
        r.setAddress("Testvej 1, 2000 Frederiksberg");
        r.setPhone("12345678");
        return r;
    }

    // ── findAll ─────────────────────────────────────────────────────────────────

    @Test
    void findAll_returnerAlle() {
        Restaurant r1 = nyRestaurant("Pizza Palace");
        Restaurant r2 = nyRestaurant("Burger Barn");
        when(restaurantRepository.findAll()).thenReturn(List.of(r1, r2));

        List<Restaurant> result = restaurantService.findAllRestaurants();

        assertThat(result).containsExactly(r1, r2);
    }

    // ── createRestaurant ────────────────────────────────────────────────────────

    @Test
    void createRestaurant_gemmerOgReturnerer() {
        Restaurant input = nyRestaurant("Sushi Spot");
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Restaurant result = restaurantService.createRestaurant(input);

        assertThat(result.getName()).isEqualTo("Sushi Spot");
        verify(restaurantRepository).save(input);
    }

    // ── updateRestaurant ────────────────────────────────────────────────────────

    @Test
    void updateRestaurant_opdatererKunIkkeNullFelter() {
        Restaurant existing = nyRestaurant("Gammelt Navn");
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Restaurant opdatering = new Restaurant();
        opdatering.setName("Nyt Navn");
        // address og phone er null – skal ikke overskrives

        Restaurant result = restaurantService.updateRestaurant(1L, opdatering);

        assertThat(result.getName()).isEqualTo("Nyt Navn");
        assertThat(result.getAddress()).isEqualTo("Testvej 1, 2000 Frederiksberg");
        assertThat(result.getPhone()).isEqualTo("12345678");
    }

    @Test
    void updateRestaurant_ikkeFound_kasterNotFound() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.updateRestaurant(99L, new Restaurant()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── deleteRestaurant ────────────────────────────────────────────────────────

    @Test
    void deleteRestaurant_sletterHvisFound() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);

        restaurantService.deleteRestaurant(1L);

        verify(restaurantRepository).deleteById(1L);
    }

    @Test
    void deleteRestaurant_ikkeFound_kasterNotFound() {
        when(restaurantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.deleteRestaurant(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteRestaurant_medTilknytedeAnmodninger_kasterConflict() {
        // FK-constraint i databasen kaster DataIntegrityViolationException
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(restaurantRepository).deleteById(1L);

        assertThatThrownBy(() -> restaurantService.deleteRestaurant(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
