package com.petopia.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$",
             message = "Username must be 3-30 chars: letters, digits or underscore only")
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 200, message = "Display name max 200 chars")
    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ─────────────────────────────────────────
    public User() {}

    public User(String username, String passwordHash, String displayName) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.displayName  = displayName;
    }

    // ── Getters / Setters ────────────────────────────────────
    public Long getId()                        { return id; }
    public String getUsername()                { return username; }
    public void   setUsername(String v)        { this.username = v; }
    public String getPasswordHash()            { return passwordHash; }
    public void   setPasswordHash(String v)    { this.passwordHash = v; }
    public String getDisplayName()             { return displayName; }
    public void   setDisplayName(String v)     { this.displayName = v; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
}
