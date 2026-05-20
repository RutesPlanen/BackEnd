package dk.rutesplanen.backend.controller;

import dk.rutesplanen.backend.model.Pickup;
import dk.rutesplanen.backend.service.PickupService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-controller til afhentninger.
 */
@RestController
@RequestMapping("/api/pickups")
public class PickupController {

    private final PickupService pickupService;

    public PickupController(PickupService pickupService) {
        this.pickupService = pickupService;
    }

    @GetMapping
    public List<Pickup> getAllPickups() {
        return pickupService.findAllPickups();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pickup registerPickup(@RequestBody Pickup pickup) {
        return pickupService.registerPickup(pickup);
    }
}
