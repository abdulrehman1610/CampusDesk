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
import android.widget.RadioGroup;
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
 * LoginActivity handles both Student and Admin sign-in from a single screen (PRD Section 5.1 A3-A5).
 * A top segmented toggle pill switches between Student and Admin mode, hides registration in Admin
 * mode, enforces role matching against SQLite, saves the session in SharedPreferences, and routes
 * to UserDashboardActivity or AdminDashboardActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private TextView tvLoginTitle;
    private TextView tvLoginSubtitle;
    private RadioGroup rgLoginRole;
    private EditText etEmail;
    private EditText etPassword;
    private ImageView ivTogglePassword;
    private CheckBox cbRememberMe;
    private CheckBox cbShowPassword;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private View layoutRegisterRow;
    private View layoutCreateAccount;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isPasswordVisible = false;
    private String selectedRole = Constants.ROLE_USER;

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
        updateRoleMode(Constants.ROLE_USER);
    }

    /**
     * Connects Java fields to XML views using findViewById (PRD Section 2).
     */
    private void bindViews() {
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvLoginSubtitle = findViewById(R.id.tvLoginSubtitle);
        rgLoginRole = findViewById(R.id.rgLoginRole);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        layoutRegisterRow = findViewById(R.id.layoutRegisterRow);
        layoutCreateAccount = findViewById(R.id.layoutCreateAccount);
    }

    /**
     * Attaches role toggle, password visibility, login, and registration listeners.
     */
    private void setupListeners() {
        // Switch between Student and Admin login modes
        rgLoginRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRoleAdmin) {
                updateRoleMode(Constants.ROLE_ADMIN);
            } else {
                updateRoleMode(Constants.ROLE_USER);
            }
        });

        // Show/hide password via the CheckBox (PRD A4)
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setPasswordVisibility(isChecked);
        });

        // Show/hide password via the eye icon inside the password box
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

        // Navigate to RegisterActivity (Student mode only)
        layoutCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Updates the screen title, subtitle, email hint, button text, and registration visibility
     * when the user toggles between Student and Admin login.
     */
    private void updateRoleMode(String role) {
        selectedRole = role;
        etEmail.setError(null);
        etPassword.setError(null);

        if (Constants.ROLE_ADMIN.equals(role)) {
            tvLoginTitle.setText(R.string.login_hero_title_admin);
            tvLoginSubtitle.setText(R.string.login_hero_subtitle_admin);
            etEmail.setHint(R.string.login_hint_email_admin);
            btnLogin.setText(R.string.login_button_sign_in_admin);
            layoutRegisterRow.setVisibility(View.GONE);
        } else {
            tvLoginTitle.setText(R.string.login_hero_title_student);
            tvLoginSubtitle.setText(R.string.login_hero_subtitle);
            etEmail.setHint(R.string.login_hint_email);
            btnLogin.setText(R.string.login_button_sign_in_student);
            layoutRegisterRow.setVisibility(View.VISIBLE);
        }
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
     * Validates email/password, checks SQLite credentials, enforces that the account's role
     * matches the selected Student/Admin toggle, saves the session, and opens the dashboard.
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

        // Check database credentials
        User user = dbHelper.loginUser(email, password);
        if (user == null) {
            Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        // Enforce that the user's database role matches the selected Student/Admin login portal
        if (!selectedRole.equals(user.getRole())) {
            if (Constants.ROLE_ADMIN.equals(selectedRole)) {
                Toast.makeText(this, R.string.login_error_wrong_role_admin, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.login_error_wrong_role_student, Toast.LENGTH_LONG).show();
            }
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
