package com.petopia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battles")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nullable — guest battles have no user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = true)
    private User player;

    @Column(name = "opponent_name", length = 200)
    private String opponentName;

    // "WIN", "LOSS", or "FLED"
    @Column(name = "winner", length = 50)
    private String winner;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ─────────────────────────────────────────
    public Battle() {}

    public Battle(User player, String opponentName, String winner, long durationMs) {
        this.player       = player;
        this.opponentName = opponentName;
        this.winner       = winner;
        this.durationMs   = durationMs;
    }

    // ── Getters / Setters ────────────────────────────────────
    public Long          getId()                      { return id; }
    public User          getPlayer()                  { return player; }
    public void          setPlayer(User v)            { this.player = v; }
    public String        getOpponentName()            { return opponentName; }
    public void          setOpponentName(String v)    { this.opponentName = v; }
    public String        getWinner()                  { return winner; }
    public void          setWinner(String v)          { this.winner = v; }
    public Long          getDurationMs()              { return durationMs; }
    public void          setDurationMs(Long v)        { this.durationMs = v; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
}
