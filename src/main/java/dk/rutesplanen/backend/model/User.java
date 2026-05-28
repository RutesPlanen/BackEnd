package dk.rutesplanen.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA-entitet for brugere i RutesPlanen.
 */
@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    // Adgangskoden sendes aldrig tilbage til klienten i JSON-svaret
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    // Rollen gemmes som tekst i databasen (ADMIN, RESTAURANT eller CHAUFFEUR)
    @Enumerated(EnumType.STRING)
    private Role role;

    // Kun relevant for RESTAURANT-brugere – linker brugeren til sin restaurant
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // null eller true = aktiv, false = deaktiveret (brugere slettes aldrig for at bevare historik)
    private Boolean active;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}