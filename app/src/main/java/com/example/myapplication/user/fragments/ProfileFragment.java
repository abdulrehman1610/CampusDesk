package com.example.myapplication.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

/**
 * ProfileFragment is the fourth bottom-navigation tab on UserDashboardActivity.
 * In Milestone M4 it acts as a placeholder; in Milestone M7 it will display the
 * student's profile details, complaint statistics, change-password dialog, and logout button.
 */
public class ProfileFragment extends Fragment {

    /**
     * Inflates fragment_profile.xml for the Profile tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}
