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
 * UsersFragment is the fourth tab on the Admin dashboard.
 * In Milestone M3 it acts as a placeholder tab; in Milestone M6 it will display
 * all registered users in a ListView with a custom BaseAdapter and delete button.
 */
public class UsersFragment extends Fragment {

    /**
     * Inflates fragment_users.xml for the Users tab.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }
}
