package dk.rutesplanen.backend.service;

import dk.rutesplanen.backend.model.PickupRequest;
import dk.rutesplanen.backend.model.PickupStatus;
import dk.rutesplanen.backend.repositories.PickupRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickupRequestServiceTest {

    @Mock
    private PickupRequestRepository pickupRequestRepository;

    private PickupRequestService pickupRequestService;

    @BeforeEach
    void setUp() {
        pickupRequestService = new PickupRequestService(pickupRequestRepository);
    }

    private PickupRequest anmodningMedStatus(PickupStatus status) {
        PickupRequest req = new PickupRequest();
        req.setId(1L);
        req.setStatus(status);
        req.setPantAmount(10);
        return req;
    }

    // ── createPickupRequest ─────────────────────────────────────────────────────

    @Test
    void create_setterPendingNaarStatusErNull() {
        PickupRequest req = new PickupRequest();
        req.setStatus(null);
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest result = pickupRequestService.createPickupRequest(req);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.PENDING);
    }

    @Test
    void create_beholderEksisterendeStatus() {
        PickupRequest req = new PickupRequest();
        req.setStatus(PickupStatus.PLANNED);
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest result = pickupRequestService.createPickupRequest(req);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.PLANNED);
    }

    // ── updatePickupRequest – statusovergange ───────────────────────────────────

    @Test
    void update_pendingTilPlanned_erGyldig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PENDING);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.PLANNED);

        PickupRequest result = pickupRequestService.updatePickupRequest(1L, opdatering);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.PLANNED);
    }

    @Test
    void update_plannedTilCompleted_saetterCompletedAt() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PLANNED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.COMPLETED);

        PickupRequest result = pickupRequestService.updatePickupRequest(1L, opdatering);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void update_pendingTilCompleted_erGyldig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PENDING);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.COMPLETED);

        PickupRequest result = pickupRequestService.updatePickupRequest(1L, opdatering);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.COMPLETED);
    }

    @Test
    void update_completedTilPending_erUgyldig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.COMPLETED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.PENDING);

        assertThatThrownBy(() -> pickupRequestService.updatePickupRequest(1L, opdatering))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void update_completedTilPlanned_erUgyldig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.COMPLETED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.PLANNED);

        assertThatThrownBy(() -> pickupRequestService.updatePickupRequest(1L, opdatering))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void update_plannedTilPending_erUgyldig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PLANNED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.PENDING);

        assertThatThrownBy(() -> pickupRequestService.updatePickupRequest(1L, opdatering))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void update_sammeStatus_erLovlig() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PLANNED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.PLANNED);

        PickupRequest result = pickupRequestService.updatePickupRequest(1L, opdatering);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.PLANNED);
    }

    @Test
    void update_completedAtOverskorivesIkkeNaarAlleredesat() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PLANNED);
        var tidligereCompletedAt = java.time.LocalDateTime.of(2025, 1, 1, 12, 0);
        existing.setCompletedAt(tidligereCompletedAt);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest opdatering = new PickupRequest();
        opdatering.setStatus(PickupStatus.COMPLETED);

        PickupRequest result = pickupRequestService.updatePickupRequest(1L, opdatering);

        assertThat(result.getCompletedAt()).isEqualTo(tidligereCompletedAt);
    }

    @Test
    void update_ikkeFound_kasterNotFound() {
        when(pickupRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pickupRequestService.updatePickupRequest(99L, new PickupRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── markerAfhentet ──────────────────────────────────────────────────────────

    @Test
    void markerAfhentet_saetterCompletedOgTimestamp() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.PLANNED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pickupRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PickupRequest result = pickupRequestService.markerAfhentet(1L);

        assertThat(result.getStatus()).isEqualTo(PickupStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void markerAfhentet_alleredeCompleted_kasterConflict() {
        PickupRequest existing = anmodningMedStatus(PickupStatus.COMPLETED);
        when(pickupRequestRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> pickupRequestService.markerAfhentet(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void markerAfhentet_ikkeFound_kasterNotFound() {
        when(pickupRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pickupRequestService.markerAfhentet(5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── deletePickupRequest ─────────────────────────────────────────────────────

    @Test
    void delete_ikkeFound_kasterNotFound() {
        when(pickupRequestRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> pickupRequestService.deletePickupRequest(7L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_sletterAnmodning() {
        when(pickupRequestRepository.existsById(1L)).thenReturn(true);

        pickupRequestService.deletePickupRequest(1L);

        verify(pickupRequestRepository).deleteById(1L);
    }
}
