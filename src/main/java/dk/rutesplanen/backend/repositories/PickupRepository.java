package dk.rutesplanen.backend.repositories;

import dk.rutesplanen.backend.model.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository giver automatisk findAll, findById, save, delete m.m. uden at skrive SQL
public interface PickupRepository extends JpaRepository<Pickup, Long> {
}
