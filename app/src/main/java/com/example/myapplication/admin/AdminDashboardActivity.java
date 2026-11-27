package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.AdminPagerAdapter;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * AdminDashboardActivity hosts the 4 admin tabs (Overview, Complaints, Notices, Users)
 * using TabLayout + ViewPager2 + AdminPagerAdapter (FragmentStateAdapter) as required by PRD D1.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private TextView tvHeaderComplaintCount;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    /**
     * Verifies the admin session, sets up the Toolbar, and connects TabLayout to ViewPager2.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Role check required by PRD Section 4: only "admin" role can open this dashboard
        if (!sessionManager.isLoggedIn() || !Constants.ROLE_ADMIN.equals(sessionManager.getRole())) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);

        tvHeaderComplaintCount = findViewById(R.id.tvHeaderComplaintCount);
        tabLayout = findViewById(R.id.tabLayoutAdmin);
        viewPager = findViewById(R.id.viewPagerAdmin);

        setupViewPagerAndTabs();
        updateHeaderSummary();
    }

    /**
     * Refreshes the header complaint count whenever the admin returns to this activity.
     */
    @Override
    public void onResume() {
        super.onResume();
        updateHeaderSummary();
    }

    /**
     * Connects ViewPager2 to AdminPagerAdapter and links TabLayout titles via TabLayoutMediator.
     */
    private void setupViewPagerAndTabs() {
        AdminPagerAdapter pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == AdminPagerAdapter.TAB_OVERVIEW) {
                tab.setText(R.string.tab_overview);
            } else if (position == AdminPagerAdapter.TAB_COMPLAINTS) {
                tab.setText(R.string.tab_complaints);
            } else if (position == AdminPagerAdapter.TAB_NOTICES) {
                tab.setText(R.string.tab_notices);
            } else if (position == AdminPagerAdapter.TAB_USERS) {
                tab.setText(R.string.tab_users);
            }
        }).attach();
    }

    /**
     * Switches ViewPager2 to the specified tab index (used by Overview shortcuts in M6).
     */
    public void switchToTab(int tabIndex) {
        if (viewPager != null && tabIndex >= 0 && tabIndex < AdminPagerAdapter.TAB_COUNT) {
            viewPager.setCurrentItem(tabIndex, true);
        }
    }

    /**
     * Updates the header micro-bar with the total number of recorded complaints from SQLite.
     */
    public void updateHeaderSummary() {
        if (tvHeaderComplaintCount != null && dbHelper != null) {
            int totalComplaints = dbHelper.getComplaintCount();
            tvHeaderComplaintCount.setText(getString(R.string.admin_header_recorded_count, totalComplaints));
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
    private void performLogout() {
        sessionManager.logout();
        Toast.makeText(this, R.string.logout_success_toast, Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    /**
     * Opens LoginActivity and clears the activity back stack.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
