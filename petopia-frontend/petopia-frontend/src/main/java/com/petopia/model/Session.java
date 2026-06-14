package com.petopia.model;

/**
 * Client-side session — stores the logged-in user's details and auth token
 * returned by the backend after a successful login.
 */
public class Session {

    private static Long   userId;
    private static String username;
    private static String displayName;
    private static String token;       // X-Auth-Token for API calls

    public static void login(Long userIdValue, String usernameValue,
                             String displayNameValue, String tokenValue) {
        userId      = userIdValue;
        username    = usernameValue;
        displayName = displayNameValue;
        token       = tokenValue;
    }

    public static void logout() {
        userId      = null;
        username    = null;
        displayName = null;
        token       = null;
    }

    public static boolean isLoggedIn()      { return userId != null; }
    public static Long    getUserId()       { return userId; }
    public static String  getUsername()     { return username; }
    public static String  getDisplayName()  { return displayName; }
    public static String  getToken()        { return token; }
}
