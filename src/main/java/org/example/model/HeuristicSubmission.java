package org.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class HeuristicSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Több beküldés is tartozhat egy felhasználóhoz
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false) // TEXT, hogy elférjen a hosszú kód
    private String code;

    // Értékelési metrikák a ranglistához
    private Integer stepsToSolve;     // Hány lépés a megoldás (minél kevesebb, annál jobb)
    private Long executionTimeMs;     // Futási idő

    private LocalDateTime submissionTime = LocalDateTime.now();

    public HeuristicSubmission() {}

    public HeuristicSubmission(User user, String code, Integer stepsToSolve, Long executionTimeMs) {
        this.user = user;
        this.code = code;
        this.stepsToSolve = stepsToSolve;
        this.executionTimeMs = executionTimeMs;
    }

    // Getterek
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCode() { return code; }
    public Integer getStepsToSolve() { return stepsToSolve; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }
}