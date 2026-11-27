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
 * OverviewFragment is the first tab on the Admin dashboard.
 * In Milestone M3 it acts as a placeholder tab; in Milestone M6 it will display
 * SQL COUNT(*) stat cards, the SLA progress bar, and Recent Filings.
 */
public class OverviewFragment extends Fragment {

    /**
     * Inflates fragment_overview.xml for the Overview tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }
}
