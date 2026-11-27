package com.example.myapplication.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

/**
 * AdminComplaintsFragment is the second tab on the Admin dashboard.
 * In Milestone M3 it acts as a placeholder tab; in Milestone M6 it will display
 * all student complaints with status filter, sort Spinner, and reply/resolve actions.
 */
public class AdminComplaintsFragment extends Fragment {

    /**
     * Inflates fragment_admin_complaints.xml for the Complaints tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_complaints, container, false);
    }
}
