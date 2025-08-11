package com.example.algorithmvisualizerfrontend.util;

public class SessionContext {
    private static String token;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        SessionContext.token = token;
    }

    public static boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    public static void clear(){
        token = null;
    }
}
