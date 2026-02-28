package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user") // A "user" sok adatbázisban védett szó, ezért átnevezzük a táblát
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // Üres konstruktor a JPA-nak
    public User() {}

    public User(String username) {
        this.username = username;
    }

    // Getterek és Setterek
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}