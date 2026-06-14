package com.petopia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$",
             message = "Username must be 3-30 chars: letters, digits or underscore only")
    private String username;

    @Size(max = 200, message = "Display name max 200 chars")
    private String displayName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be 8-128 chars")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
             message = "Password must contain uppercase, lowercase, digit and symbol")
    private String password;

    // ── Getters / Setters ────────────────────────────────────
    public String getUsername()                 { return username; }
    public void   setUsername(String v)         { this.username = v; }
    public String getDisplayName()              { return displayName; }
    public void   setDisplayName(String v)      { this.displayName = v; }
    public String getPassword()                 { return password; }
    public void   setPassword(String v)         { this.password = v; }
}
