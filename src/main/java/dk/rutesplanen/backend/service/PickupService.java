package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.Pickup;
import dk.rutesplanen.backend.repositories.PickupRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service til registrering af afhentninger.
 */
@Service
public class PickupService {

    private final PickupRepository pickupRepository;

    public PickupService(PickupRepository pickupRepository) {
        this.pickupRepository = pickupRepository;
    }

    public List<Pickup> findAllPickups() {
        return pickupRepository.findAll();
    }

    public Pickup registerPickup(Pickup pickup) {
        return pickupRepository.save(pickup);
    }
}
