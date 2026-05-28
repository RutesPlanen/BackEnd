package dk.rutesplanen.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * JPA-entitet for chauffør-udgifter (benzin, parkering, m.m.).
 */
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Forbinder udgiften til den chauffør der registrerede den
    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private User chauffeur;

    private Double amount;
    private String category;
    private String description;

    // Billedet gemmes som base64-streng direkte i databasen (undgår filsystem-kompleksitet i Docker)
    @Column(columnDefinition = "TEXT")
    private String imageData;

    private LocalDateTime createdAt;

    // Sætter oprettelsestidspunktet automatisk lige inden rækken gemmes i databasen
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now(ZoneId.of("Europe/Copenhagen"));
    }

    public Expense() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getChauffeur() { return chauffeur; }
    public void setChauffeur(User chauffeur) { this.chauffeur = chauffeur; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}