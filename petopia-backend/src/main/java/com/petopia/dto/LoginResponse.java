package com.petopia.dto;

public class LoginResponse {

    private Long   userId;
    private String username;
    private String displayName;
    private String token;   // simple session token

    public LoginResponse(Long userId, String username, String displayName, String token) {
        this.userId      = userId;
        this.username    = username;
        this.displayName = displayName;
        this.token       = token;
    }

    public Long   getUserId()      { return userId; }
    public String getUsername()    { return username; }
    public String getDisplayName() { return displayName; }
    public String getToken()       { return token; }
}
