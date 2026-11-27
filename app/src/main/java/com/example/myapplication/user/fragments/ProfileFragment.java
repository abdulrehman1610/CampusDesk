package com.example.myapplication.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.User;
import com.example.myapplication.user.UserDashboardActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

/**
 * ProfileFragment displays the logged-in student's profile (Figma 2:956 & PRD Section 5.2 U10).
 * It shows the student's initials avatar, full name, email, live complaint statistics,
 * a Ticket Alerts preference switch (saved in SharedPreferences), a Change Password dialog
 * that updates SQLite, and a Sign Out button.
 */
public class ProfileFragment extends Fragment {

    private TextView tvInitials;
    private TextView tvName;
    private TextView tvEmail;
    private View cardInReview;
    private View cardResolved;
    private TextView tvPendingCount;
    private TextView tvResolvedCount;
    private TextView tvResolvedRate;
    private SwitchMaterial switchAlerts;
    private View layoutChangePassword;
    private View layoutStudentCharter;
    private Button btnSignOut;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    /**
     * Inflates fragment_profile.xml, binds views, and sets up click listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        bindViews(view);
        setupListeners();

        return view;
    }

    /**
     * Refreshes the student's profile details and complaint stats whenever the tab is shown.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadProfileAndStats();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews(View view) {
        tvInitials = view.findViewById(R.id.tvProfileInitials);
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        cardInReview = view.findViewById(R.id.cardProfileInReview);
        cardResolved = view.findViewById(R.id.cardProfileResolved);
        tvPendingCount = view.findViewById(R.id.tvProfilePendingCount);
        tvResolvedCount = view.findViewById(R.id.tvProfileResolvedCount);
        tvResolvedRate = view.findViewById(R.id.tvProfileResolvedRate);
        switchAlerts = view.findViewById(R.id.switchTicketAlerts);
        layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        layoutStudentCharter = view.findViewById(R.id.layoutStudentCharter);
        btnSignOut = view.findViewById(R.id.btnProfileSignOut);
    }

    /**
     * Loads the student's name, email, and complaint counts from SessionManager and SQLite.
     */
    private void loadProfileAndStats() {
        int userId = sessionManager.getUserId();
        User user = dbHelper.getUserById(userId);

        String name = (user != null) ? user.getName() : sessionManager.getName();
        String email = (user != null) ? user.getEmail() : sessionManager.getEmail();

        tvInitials.setText(extractInitials(name));
        tvName.setText(name);
        tvEmail.setText(email);

        int total = dbHelper.getComplaintCountByUser(userId);
        int pending = dbHelper.getComplaintCountByUserAndStatus(userId, Constants.STATUS_PENDING);
        int resolved = dbHelper.getComplaintCountByUserAndStatus(userId, Constants.STATUS_RESOLVED);
        int rate = total > 0 ? Math.round((resolved * 100f) / total) : 100;

        tvPendingCount.setText(String.format(Locale.US, "%02d", pending));
        tvResolvedCount.setText(String.format(Locale.US, "%02d", resolved));
        tvResolvedRate.setText(String.format(Locale.US, "%d%%", rate));

        switchAlerts.setChecked(sessionManager.isAlertsEnabled());
    }

    /**
     * Attaches listeners for the stat cards, Ticket Alerts switch, Change Password dialog,
     * Student Charter dialog, and Sign Out button.
     */
    private void setupListeners() {
        View.OnClickListener openMyComplaints = v -> {
            if (getActivity() instanceof UserDashboardActivity) {
                ((UserDashboardActivity) getActivity()).navigateToTab(R.id.nav_my_complaints);
            }
        };
        cardInReview.setOnClickListener(openMyComplaints);
        cardResolved.setOnClickListener(openMyComplaints);

        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setAlertsEnabled(isChecked);
            int msg = isChecked ? R.string.profile_alerts_on_toast : R.string.profile_alerts_off_toast;
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        layoutStudentCharter.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.profile_charter_title)
                    .setMessage(R.string.profile_charter_dialog_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

        btnSignOut.setOnClickListener(v -> {
            if (getActivity() instanceof UserDashboardActivity) {
                ((UserDashboardActivity) getActivity()).performLogout();
            }
        });
    }

    /**
     * Shows an AlertDialog with Current Password, New Password, and Confirm New Password fields,
     * validates them, and calls DatabaseHelper.updatePassword() (PRD U10).
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null, false);

        EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmNewPassword);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_password_heading)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String currentPass = etCurrent.getText().toString();
                String newPass = etNew.getText().toString();
                String confirmPass = etConfirm.getText().toString();

                int userId = sessionManager.getUserId();
                User user = dbHelper.getUserById(userId);
                if (user == null) {
                    dialog.dismiss();
                    return;
                }

                if (currentPass.isEmpty() || !currentPass.equals(user.getPassword())) {
                    etCurrent.setError(getString(R.string.error_current_password_wrong));
                    etCurrent.requestFocus();
                    return;
                }

                if (newPass.length() < Constants.MIN_PASSWORD_LENGTH) {
                    etNew.setError(getString(R.string.error_password_short));
                    etNew.requestFocus();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    etConfirm.setError(getString(R.string.error_password_mismatch));
                    etConfirm.requestFocus();
                    return;
                }

                boolean updated = dbHelper.updatePassword(userId, newPass);
                if (updated) {
                    Toast.makeText(requireContext(), R.string.password_updated_toast, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * Extracts up to 2 uppercase initials from a full name (e.g. "Hamza Ali" -> "HA").
     */
    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.US);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.US);
    }
}
