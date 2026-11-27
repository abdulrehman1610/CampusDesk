package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.user.UserDashboardActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

/**
 * MainActivity acts as the launcher and session router (PRD Section 5.1 A7).
 * When the app opens, it reads SharedPreferences via SessionManager:
 * - If "Remember me" is active, it forwards straight to the User or Admin dashboard.
 * - Otherwise, it clears any temporary session and opens LoginActivity.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Checks the saved login session on launch and routes to the right screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        Intent targetIntent;

        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            String role = sessionManager.getRole();
            if (Constants.ROLE_ADMIN.equals(role)) {
                targetIntent = new Intent(this, AdminDashboardActivity.class);
            } else if (Constants.ROLE_USER.equals(role)) {
                targetIntent = new Intent(this, UserDashboardActivity.class);
            } else {
                sessionManager.logout();
                targetIntent = new Intent(this, LoginActivity.class);
            }
        } else {
            // If "Remember me" was not checked, require sign-in on a fresh app launch
            sessionManager.logout();
            targetIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(targetIntent);
        finish();
    }
}
