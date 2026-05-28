package dk.rutesplanen.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * JPA-entitet for pantanmodninger.
 */
@Entity
public class PickupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kobler anmodningen til den restaurant der ønsker afhentning
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private LocalDate date;
    private Integer pantAmount;
    // Status gemmes som tekst: PENDING → PLANNED → COMPLETED
    @Enumerated(EnumType.STRING)
    private PickupStatus status;
    // Sættes automatisk i service-laget når status skifter til COMPLETED
    private java.time.LocalDateTime completedAt;

    public PickupRequest() {
        // Standardkonstruktør til JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getPantAmount() {
        return pantAmount;
    }

    public void setPantAmount(Integer pantAmount) {
        this.pantAmount = pantAmount;
    }

    public PickupStatus getStatus() {
        return status;
    }

    public void setStatus(PickupStatus status) {
        this.status = status;
    }

    public java.time.LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(java.time.LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
