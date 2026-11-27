package com.example.myapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.User;
import com.example.myapplication.user.UserDashboardActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

/**
 * LoginActivity handles user and admin sign-in (PRD Section 5.1 A3, A4, A5).
 * It validates credentials against SQLite, saves the session in SharedPreferences,
 * and routes to either UserDashboardActivity or AdminDashboardActivity based on role.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private ImageView ivTogglePassword;
    private CheckBox cbRememberMe;
    private CheckBox cbShowPassword;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private View layoutCreateAccount;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isPasswordVisible = false;

    /**
     * Initializes the Login UI views, database helper, session manager, and click listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        bindViews();
        setupListeners();
    }

    /**
     * Connects Java fields to XML views using findViewById (PRD Section 2).
     */
    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        layoutCreateAccount = findViewById(R.id.layoutCreateAccount);
    }

    /**
     * Attaches click and check-change listeners to buttons, checkboxes, and links.
     */
    private void setupListeners() {
        // Show/hide password via the CheckBox (PRD A4)
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setPasswordVisibility(isChecked);
        });

        // Show/hide password via the eye icon inside the password box (Figma design)
        ivTogglePassword.setOnClickListener(v -> {
            boolean newState = !isPasswordVisible;
            cbShowPassword.setChecked(newState);
            setPasswordVisibility(newState);
        });

        // Forgot password helper message
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, R.string.login_forgot_password_toast, Toast.LENGTH_SHORT).show();
        });

        // Primary Sign In button
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigate to RegisterActivity
        layoutCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Toggles the password EditText between hidden dots and visible plain text.
     */
    private void setPasswordVisibility(boolean visible) {
        isPasswordVisible = visible;
        if (visible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        // Keep the typing cursor at the end of the text
        etPassword.setSelection(etPassword.getText().length());
    }

    /**
     * Validates the email and password fields, checks SQLite, saves the session,
     * and opens the appropriate dashboard via an explicit Intent.
     */
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return;
        }

        // Check database
        User user = dbHelper.loginUser(email, password);
        if (user == null) {
            Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save session in SharedPreferences (including Remember Me flag)
        boolean rememberMe = cbRememberMe.isChecked();
        sessionManager.saveLogin(user.getId(), user.getName(), user.getEmail(), user.getRole(), rememberMe);

        Toast.makeText(this, getString(R.string.login_welcome_user, user.getName()), Toast.LENGTH_SHORT).show();

        // Route to Admin or User dashboard based on role
        Intent intent;
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
        }

        // Clear back stack so pressing Back from the dashboard does not return to Login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
