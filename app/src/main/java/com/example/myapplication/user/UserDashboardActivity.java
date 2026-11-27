package com.example.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * UserDashboardActivity is the home screen for logged-in students.
 * In Milestone M2 it verifies the "user" role, displays the logged-in student's info,
 * and supports Logout from the Toolbar options menu (PRD A6).
 * In Milestone M4 it will host the 4 bottom-navigation fragments.
 */
public class UserDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    /**
     * Checks the user session and role, sets up the Toolbar, and binds the M2 shell views.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        // Role check required by PRD Section 4: only "user" role can open this dashboard
        if (!sessionManager.isLoggedIn() || !Constants.ROLE_USER.equals(sessionManager.getRole())) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_user_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);

        TextView tvWelcome = findViewById(R.id.tvUserWelcome);
        TextView tvEmail = findViewById(R.id.tvUserEmail);
        Button btnLogout = findViewById(R.id.btnUserLogout);

        tvWelcome.setText(getString(R.string.login_welcome_user, sessionManager.getName()));
        tvEmail.setText(sessionManager.getEmail());

        btnLogout.setOnClickListener(v -> performLogout());
    }

    /**
     * Inflates the Toolbar options menu containing the Logout action (PRD Section 3 & A6).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    /**
     * Handles clicks on the Toolbar menu items (Logout).
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Clears the saved session in SharedPreferences and returns to LoginActivity.
     */
    private void performLogout() {
        sessionManager.logout();
        Toast.makeText(this, R.string.logout_success_toast, Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    /**
     * Opens LoginActivity and clears the activity back stack.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(UserDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
