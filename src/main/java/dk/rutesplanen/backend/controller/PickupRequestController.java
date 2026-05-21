package dk.rutesplanen.backend.controller;

import dk.rutesplanen.backend.model.PickupRequest;
import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.service.LoginTokenService;
import dk.rutesplanen.backend.service.PickupRequestService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST-controller til pantanmodninger.
 */
@RestController
@RequestMapping("/api/pickup-requests")
public class PickupRequestController {

    private final PickupRequestService pickupRequestService;
    private final LoginTokenService loginTokenService;

    public PickupRequestController(PickupRequestService pickupRequestService,
                                   LoginTokenService loginTokenService) {
        this.pickupRequestService = pickupRequestService;
        this.loginTokenService = loginTokenService;
    }

    @GetMapping
    public List<PickupRequest> getAllPickupRequests(
            @RequestParam(required = false) PickupStatus status) {
        return pickupRequestService.findAllPickupRequests(status);
    }

    // Returnerer kun anmodninger for den restaurant som den indloggede bruger er tilknyttet
    @GetMapping("/mine")
    public List<PickupRequest> getMyPickupRequests(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRestaurant() == null) {
            return List.of();
        }
        return pickupRequestService.findByRestaurant(user.getRestaurant());
    }

    @GetMapping("/{id}")
    public PickupRequest getPickupRequestById(@PathVariable Long id) {
        return pickupRequestService.findPickupRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anmodning ikke fundet"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PickupRequest createPickupRequest(@RequestBody PickupRequest pickupRequest,
                                             HttpServletRequest request) {
        User user = getCurrentUser(request);
        // Restaurant-brugere kan ikke selv vælge restaurant – den hentes automatisk fra deres profil
        if (user.getRole() == Role.RESTAURANT) {
            if (user.getRestaurant() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Din brugerkonto er ikke tilknyttet en restaurant");
            }
            pickupRequest.setRestaurant(user.getRestaurant());
        }
        return pickupRequestService.createPickupRequest(pickupRequest);
    }

    @PutMapping("/{id}")
    public PickupRequest updatePickupRequest(@PathVariable Long id,
                                             @RequestBody PickupRequest opdateret) {
        return pickupRequestService.updatePickupRequest(id, opdateret);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePickupRequest(@PathVariable Long id) {
        pickupRequestService.deletePickupRequest(id);
    }

    // Bruges af chaufføren på dashboardet til at markere en afhentning som udført
    @PatchMapping("/{id}/afhentet")
    public PickupRequest markerAfhentet(@PathVariable Long id) {
        return pickupRequestService.markerAfhentet(id);
    }

    // Læser Bearer-token fra headeren og returnerer den tilhørende bruger
    private User getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return loginTokenService.validerToken(header.substring(7))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
