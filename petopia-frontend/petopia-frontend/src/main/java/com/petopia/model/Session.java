package com.petopia.model;

public class Session {
    private static Long userId;
    private static String username;
    private static String displayName;

    public static void login(Long userIdValue, String usernameValue, String displayNameValue) {
        userId = userIdValue;
        username = usernameValue;
        displayName = displayNameValue;
    }

    public static void logout() {
        userId = null;
        username = null;
        displayName = null;
    }

    public static boolean isLoggedIn() {
        return userId != null;
    }

    public static Long getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getDisplayName() {
        return displayName;
    }
}