package com.example.myapplication.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.utils.Constants;

/**
 * RegisterActivity lets a new student create an account (PRD Section 5.1 A1, A2).
 * All new accounts are stored in SQLite with the "user" role.
 */
public class RegisterActivity extends AppCompatActivity {

    private View btnBack;
    private View btnHelp;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirmPassword;
    private View viewStrength1;
    private View viewStrength2;
    private View viewStrength3;
    private View viewStrength4;
    private TextView tvStrengthLabel;
    private Button btnRegister;
    private TextView tvSignIn;

    private DatabaseHelper dbHelper;
    private boolean isPasswordVisible = false;
    private boolean isConfirmVisible = false;

    /**
     * Initializes the registration screen, views, and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        bindViews();
        setupListeners();
        updatePasswordStrengthIndicator("");
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHelp = findViewById(R.id.btnHelp);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        viewStrength1 = findViewById(R.id.viewStrength1);
        viewStrength2 = findViewById(R.id.viewStrength2);
        viewStrength3 = findViewById(R.id.viewStrength3);
        viewStrength4 = findViewById(R.id.viewStrength4);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
        btnRegister = findViewById(R.id.btnRegister);
        tvSignIn = findViewById(R.id.tvSignIn);
    }

    /**
     * Sets up button clicks, password visibility toggles, and the live password strength meter.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvSignIn.setOnClickListener(v -> finish());

        btnHelp.setOnClickListener(v -> {
            Toast.makeText(this, R.string.register_help_toast, Toast.LENGTH_SHORT).show();
        });

        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            toggleFieldVisibility(etPassword, isPasswordVisible);
        });

        ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            toggleFieldVisibility(etConfirmPassword, isConfirmVisible);
        });

        // Update the 4-bar strength indicator as the student types their password
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthIndicator(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    /**
     * Toggles an EditText between hidden password dots and visible text.
     */
    private void toggleFieldVisibility(EditText editText, boolean visible) {
        if (visible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }

    /**
     * Updates the 4 pill bars and strength text (Weak / Fair / Good / Strong) based on length.
     */
    private void updatePasswordStrengthIndicator(String password) {
        int len = password.length();
        int activeBars;
        if (len == 0) {
            activeBars = 0;
            tvStrengthLabel.setText(R.string.register_min_chars);
        } else if (len < Constants.MIN_PASSWORD_LENGTH) {
            activeBars = 1;
            tvStrengthLabel.setText(R.string.register_strength_weak);
        } else if (len < 8) {
            activeBars = 2;
            tvStrengthLabel.setText(R.string.register_strength_medium);
        } else if (len < 10) {
            activeBars = 3;
            tvStrengthLabel.setText(R.string.register_strength_good);
        } else {
            activeBars = 4;
            tvStrengthLabel.setText(R.string.register_strength_strong);
        }

        viewStrength1.setBackgroundResource(activeBars >= 1 ? R.drawable.bg_button_crimson : R.drawable.bg_chip_blue);
        viewStrength2.setBackgroundResource(activeBars >= 2 ? R.drawable.bg_button_crimson : R.drawable.bg_chip_blue);
        viewStrength3.setBackgroundResource(activeBars >= 3 ? R.drawable.bg_button_crimson : R.drawable.bg_chip_blue);
        viewStrength4.setBackgroundResource(activeBars >= 4 ? R.drawable.bg_button_crimson : R.drawable.bg_chip_blue);
    }

    /**
     * Validates all registration inputs (PRD A2) and inserts the new student into SQLite.
     */
    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // 1. No empty fields
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }

        // 2. Valid email pattern
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            etEmail.requestFocus();
            return;
        }

        // 3. Password >= 6 characters
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return;
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            etPassword.setError(getString(R.string.error_password_short));
            etPassword.requestFocus();
            return;
        }

        // 4. Passwords match
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.error_confirm_required));
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }

        // 5. Email not already used
        if (dbHelper.emailExists(email)) {
            etEmail.setError(getString(R.string.error_email_taken));
            etEmail.requestFocus();
            return;
        }

        // Save new user with role "user"
        boolean success = dbHelper.registerUser(name, email, password);
        if (success) {
            Toast.makeText(this, R.string.register_success_toast, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
