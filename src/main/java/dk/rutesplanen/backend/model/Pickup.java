package dk.rutesplanen.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

/**
 * JPA-entitet for registrerede afhentninger.
 */
@Entity
public class Pickup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Én afhentning svarer til præcis én anmodning
    @OneToOne
    @JoinColumn(name = "pickup_request_id")
    private PickupRequest pickupRequest;

    // Kobler afhentningen til den chauffør der udførte den
    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private User chauffeur;

    private Double cost;
    private String imageUrl;
    private String note;
    private LocalDateTime registeredAt;

    // Sætter registreringstidspunktet automatisk lige inden rækken gemmes i databasen
    @PrePersist
    void prePersist() {
        registeredAt = LocalDateTime.now();
    }

    public Pickup() {
        // Standardkonstruktør til JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PickupRequest getPickupRequest() {
        return pickupRequest;
    }

    public void setPickupRequest(PickupRequest pickupRequest) {
        this.pickupRequest = pickupRequest;
    }

    public User getChauffeur() {
        return chauffeur;
    }

    public void setChauffeur(User chauffeur) {
        this.chauffeur = chauffeur;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
