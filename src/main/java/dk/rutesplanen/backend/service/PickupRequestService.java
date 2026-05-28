package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.PickupRequest;
import dk.rutesplanen.backend.model.PickupStatus;
import dk.rutesplanen.backend.model.Restaurant;
import dk.rutesplanen.backend.repositories.PickupRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PickupRequestService {

    private final PickupRequestRepository pickupRequestRepository;

    public PickupRequestService(PickupRequestRepository pickupRequestRepository) {
        this.pickupRequestRepository = pickupRequestRepository;
    }

    public List<PickupRequest> findAllPickupRequests(PickupStatus status) {
        if (status != null) {
            return pickupRequestRepository.findByStatus(status);
        }
        return pickupRequestRepository.findAll();
    }

    public List<PickupRequest> findByRestaurant(Restaurant restaurant) {
        return pickupRequestRepository.findByRestaurant(restaurant);
    }

    public Optional<PickupRequest> findPickupRequestById(Long id) {
        return pickupRequestRepository.findById(id);
    }

    @Transactional
    public PickupRequest createPickupRequest(PickupRequest pickupRequest) {
        if (pickupRequest.getStatus() == null) {
            pickupRequest.setStatus(PickupStatus.PENDING);
        }
        return pickupRequestRepository.save(pickupRequest);
    }

    @Transactional
    public PickupRequest updatePickupRequest(Long id, PickupRequest opdateret) {
        PickupRequest anmodning = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anmodning ikke fundet"));
        if (opdateret.getDate() != null) anmodning.setDate(opdateret.getDate());
        if (opdateret.getPantAmount() != null) anmodning.setPantAmount(opdateret.getPantAmount());
        if (opdateret.getStatus() != null) {
            validateStatusTransition(anmodning.getStatus(), opdateret.getStatus());
            anmodning.setStatus(opdateret.getStatus());
            if (opdateret.getStatus() == PickupStatus.COMPLETED && anmodning.getCompletedAt() == null) {
                anmodning.setCompletedAt(LocalDateTime.now());
            }
        }
        return pickupRequestRepository.save(anmodning);
    }

    @Transactional
    public void deletePickupRequest(Long id) {
        if (!pickupRequestRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anmodning ikke fundet");
        }
        pickupRequestRepository.deleteById(id);
    }

    // Bruges af chauffør-dashboardet – markerer anmodning som afhentet og sætter tidsstempel
    @Transactional
    public PickupRequest markerAfhentet(Long id) {
        PickupRequest anmodning = pickupRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anmodning ikke fundet"));
        if (anmodning.getStatus() == PickupStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anmodningen er allerede markeret som afhentet");
        }
        anmodning.setStatus(PickupStatus.COMPLETED);
        if (anmodning.getCompletedAt() == null) {
            anmodning.setCompletedAt(LocalDateTime.now());
        }
        return pickupRequestRepository.save(anmodning);
    }

    // Sikrer at status kun må gå fremad: PENDING → PLANNED → COMPLETED
    private void validateStatusTransition(PickupStatus fra, PickupStatus til) {
        if (fra == til) return;
        boolean ugyldig = switch (fra) {
            case PENDING -> til != PickupStatus.PLANNED && til != PickupStatus.COMPLETED;
            case PLANNED -> til != PickupStatus.COMPLETED;
            case COMPLETED -> true;
        };
        if (ugyldig) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ugyldig statusovergang: " + fra + " → " + til);
        }
    }
}
