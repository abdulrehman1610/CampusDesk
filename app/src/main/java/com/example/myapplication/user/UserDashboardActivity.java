package com.example.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.user.fragments.MyComplaintsFragment;
import com.example.myapplication.user.fragments.NewComplaintFragment;
import com.example.myapplication.user.fragments.NoticesFragment;
import com.example.myapplication.user.fragments.ProfileFragment;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * UserDashboardActivity is the home screen for logged-in students (PRD Section 3 & 7).
 * It uses a floating BottomNavigationView dock and FragmentManager transactions to swap between
 * NoticesFragment, NewComplaintFragment, MyComplaintsFragment, and ProfileFragment.
 */
public class UserDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;

    /**
     * Verifies the student role, sets up the Toolbar and BottomNavigationView,
     * and loads NoticesFragment as the default home tab.
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

        toolbar = findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottomNavUser);
        // Keep icons and labels dead-center inside the floating dock without inset clipping or shifting.
        bottomNav.setItemHorizontalTranslationEnabled(false);
        bottomNav.setItemIconTintList(getColorStateList(R.color.bottom_nav_color));
        bottomNav.setItemTextColor(getColorStateList(R.color.bottom_nav_color));
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), 0);
            return insets;
        });

        setupBottomNavigation();

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_notices);
        }
    }

    /**
     * Listens for BottomNavigationView item selections and swaps the displayed Fragment.
     */
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment;

            if (itemId == R.id.nav_new_complaint) {
                selectedFragment = new NewComplaintFragment();
                toolbar.setSubtitle(R.string.user_subtitle_new_complaint);
            } else if (itemId == R.id.nav_my_complaints) {
                selectedFragment = new MyComplaintsFragment();
                toolbar.setSubtitle(R.string.user_subtitle_my_complaints);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                toolbar.setSubtitle(R.string.user_subtitle_profile);
            } else {
                selectedFragment = new NoticesFragment();
                toolbar.setSubtitle(R.string.user_subtitle_notices);
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerUser, selectedFragment)
                    .commit();
            return true;
        });
    }

    /**
     * Public helper that lets child fragments switch the active bottom navigation tab
     * (for example, switching to My Complaints after submitting a New Complaint in Milestone M5).
     */
    public void navigateToTab(int menuItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuItemId);
        }
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
    public void performLogout() {
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
