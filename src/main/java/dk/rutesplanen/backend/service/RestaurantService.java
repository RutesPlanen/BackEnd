package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Restaurant;
import dk.rutesplanen.backend.repositories.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service til håndtering af restauranter.
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> findRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // Opdater kun de felter der faktisk sendes – null-felter rører vi ikke
    public Restaurant updateRestaurant(Long id, Restaurant opdateret) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant ikke fundet"));
        if (opdateret.getName() != null) restaurant.setName(opdateret.getName());
        if (opdateret.getAddress() != null) restaurant.setAddress(opdateret.getAddress());
        if (opdateret.getPhone() != null) restaurant.setPhone(opdateret.getPhone());
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant ikke fundet");
        }
        try {
            restaurantRepository.deleteById(id);
        } catch (Exception e) {
            // Databasen afviser sletning hvis restauranten stadig har tilknyttede anmodninger (FK-constraint)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Restaurant kan ikke slettes – den har tilknyttede anmodninger");
        }
    }
}
