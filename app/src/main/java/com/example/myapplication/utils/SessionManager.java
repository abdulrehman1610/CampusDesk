package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager is a small wrapper around SharedPreferences.
 * It remembers who is logged in (id, name, email, role) and whether
 * "Remember me" was ticked, so the app can skip the Login screen next time.
 */
public class SessionManager {

    // Value returned by getUserId() when nobody is logged in
    public static final int NO_USER = -1;

    private final SharedPreferences prefs;

    /**
     * Opens (or creates) the app's private SharedPreferences file.
     */
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the logged-in user's details after a successful login.
     * rememberMe decides if the app should open straight to the dashboard next launch.
     */
    public void saveLogin(int userId, String name, String email, String role, boolean rememberMe) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_NAME, name);
        editor.putString(Constants.KEY_USER_EMAIL, email);
        editor.putString(Constants.KEY_USER_ROLE, role);
        editor.putBoolean(Constants.KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    /**
     * Returns the id of the logged-in user, or NO_USER (-1) if nobody is logged in.
     */
    public int getUserId() {
        return prefs.getInt(Constants.KEY_USER_ID, NO_USER);
    }

    /**
     * Returns the logged-in user's name, or an empty string.
     */
    public String getName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    /**
     * Returns the logged-in user's email, or an empty string.
     */
    public String getEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    /**
     * Returns the logged-in user's role ("user" or "admin"), or an empty string.
     */
    public String getRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "");
    }

    /**
     * Returns true if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    /**
     * Returns true if the user ticked "Remember me" when logging in.
     */
    public boolean isRememberMe() {
        return prefs.getBoolean(Constants.KEY_REMEMBER_ME, false);
    }

    /**
     * Saves the "Ticket Alerts" switch on the Profile screen.
     */
    public void setAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_ALERTS_ENABLED, enabled).apply();
    }

    /**
     * Returns the saved "Ticket Alerts" switch value (on by default).
     */
    public boolean isAlertsEnabled() {
        return prefs.getBoolean(Constants.KEY_ALERTS_ENABLED, true);
    }

    /**
     * Clears the login details. Called when the user taps Logout.
     * The alerts setting is kept because it is a device preference, not login data.
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.KEY_USER_ID);
        editor.remove(Constants.KEY_USER_NAME);
        editor.remove(Constants.KEY_USER_EMAIL);
        editor.remove(Constants.KEY_USER_ROLE);
        editor.remove(Constants.KEY_REMEMBER_ME);
        editor.apply();
    }
}
