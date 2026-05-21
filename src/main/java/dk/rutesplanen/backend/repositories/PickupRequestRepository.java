package dk.rutesplanen.backend.repositories;

import dk.rutesplanen.backend.model.PickupRequest;
import dk.rutesplanen.backend.model.PickupStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository giver automatisk findAll, findById, save, delete m.m. uden at skrive SQL
public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {
    // Spring Data genererer SQL-forespørgslen automatisk ud fra metodenavnet
    List<PickupRequest> findByStatus(PickupStatus status);
    // Bruges til at hente kun den indloggede restaurants egne anmodninger
    List<PickupRequest> findByRestaurant(dk.rutesplanen.backend.model.Restaurant restaurant);
}
