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
 * MyComplaintsFragment is the third bottom-navigation tab on UserDashboardActivity.
 * In Milestone M4 it acts as a placeholder; in Milestone M5 it will display the
 * logged-in student's complaints in a RecyclerView with colored status badges.
 */
public class MyComplaintsFragment extends Fragment {

    /**
     * Inflates fragment_my_complaints.xml for the My Complaints tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_complaints, container, false);
    }
}
