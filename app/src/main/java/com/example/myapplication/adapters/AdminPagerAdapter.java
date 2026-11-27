package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.admin.fragments.AdminComplaintsFragment;
import com.example.myapplication.admin.fragments.AdminNoticesFragment;
import com.example.myapplication.admin.fragments.OverviewFragment;
import com.example.myapplication.admin.fragments.UsersFragment;

/**
 * AdminPagerAdapter connects ViewPager2 on AdminDashboardActivity to the 4 admin fragments:
 * 0 = OverviewFragment, 1 = AdminComplaintsFragment, 2 = AdminNoticesFragment, 3 = UsersFragment (PRD D1).
 */
public class AdminPagerAdapter extends FragmentStateAdapter {

    public static final int TAB_OVERVIEW = 0;
    public static final int TAB_COMPLAINTS = 1;
    public static final int TAB_NOTICES = 2;
    public static final int TAB_USERS = 3;
    public static final int TAB_COUNT = 4;

    /**
     * Constructor that passes the hosting FragmentActivity to FragmentStateAdapter.
     */
    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Creates and returns the Fragment for the given tab position (0 to 3).
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == TAB_COMPLAINTS) {
            return new AdminComplaintsFragment();
        } else if (position == TAB_NOTICES) {
            return new AdminNoticesFragment();
        } else if (position == TAB_USERS) {
            return new UsersFragment();
        } else {
            return new OverviewFragment();
        }
    }

    /**
     * Returns the total number of tabs in the Admin dashboard (4).
     */
    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
