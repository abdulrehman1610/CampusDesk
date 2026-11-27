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
 * NewComplaintFragment is the second bottom-navigation tab on UserDashboardActivity.
 * In Milestone M4 it acts as a placeholder; in Milestone M5 it will provide the
 * GridView category picker, RadioGroup priority selector, and complaint submission form.
 */
public class NewComplaintFragment extends Fragment {

    /**
     * Inflates fragment_new_complaint.xml for the New Complaint tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_complaint, container, false);
    }
}
