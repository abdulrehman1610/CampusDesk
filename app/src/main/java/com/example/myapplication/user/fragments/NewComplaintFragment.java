package com.example.myapplication.user.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CategoryGridAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.user.UserDashboardActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

/**
 * NewComplaintFragment lets a student file a new complaint (Figma 2:253 & PRD Section 5.2 U5).
 * It uses a GridView with CategoryGridAdapter to pick the incident category and a RadioGroup
 * to select the priority (Low / Medium / High), then saves the complaint in SQLite as "Pending".
 */
public class NewComplaintFragment extends Fragment {

    private EditText etTitle;
    private TextView tvSelectedCategoryLabel;
    private GridView gvCategories;
    private RadioGroup rgPriority;
    private TextView tvCharCounter;
    private EditText etDescription;
    private Button btnSubmit;

    private CategoryGridAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    /**
     * Inflates fragment_new_complaint.xml, sets up the category GridView, and wires form listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_complaint, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        etTitle = view.findViewById(R.id.etComplaintTitle);
        tvSelectedCategoryLabel = view.findViewById(R.id.tvSelectedCategoryLabel);
        gvCategories = view.findViewById(R.id.gvCategories);
        rgPriority = view.findViewById(R.id.rgPriority);
        tvCharCounter = view.findViewById(R.id.tvCharCounter);
        etDescription = view.findViewById(R.id.etComplaintDescription);
        btnSubmit = view.findViewById(R.id.btnSubmitComplaint);

        setupCategoryGrid();
        setupListeners();
        updateCharCounter(0);

        return view;
    }

    /**
     * Initializes the 2-column GridView with CategoryGridAdapter and handles tile clicks.
     */
    private void setupCategoryGrid() {
        categoryAdapter = new CategoryGridAdapter(requireContext());
        gvCategories.setAdapter(categoryAdapter);
        updateSelectedCategoryLabel();

        gvCategories.setOnItemClickListener((parent, view, position, id) -> {
            categoryAdapter.setSelectedPosition(position);
            updateSelectedCategoryLabel();
        });
    }

    /**
     * Updates the crimson "Hostel & Mess Selected" text above the GridView.
     */
    private void updateSelectedCategoryLabel() {
        String selected = categoryAdapter.getSelectedCategory();
        tvSelectedCategoryLabel.setText(getString(R.string.new_complaint_category_selected_fmt, selected));
    }

    /**
     * Sets up the live description character counter (0/500) and the Submit Complaint button.
     */
    private void setupListeners() {
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCounter(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    /**
     * Formats and displays the current character count out of 500.
     */
    private void updateCharCounter(int count) {
        tvCharCounter.setText(getString(R.string.new_complaint_char_count_fmt, count));
    }

    /**
     * Reads the selected priority from the RadioGroup ("Low", "Medium", or "High").
     */
    private String getSelectedPriority() {
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbPriorityLow) {
            return Constants.PRIORITY_LOW;
        } else if (checkedId == R.id.rbPriorityHigh) {
            return Constants.PRIORITY_HIGH;
        } else {
            return Constants.PRIORITY_MEDIUM;
        }
    }

    /**
     * Validates the complaint form, saves the new complaint in SQLite with status "Pending",
     * and navigates to the My Complaints tab so the student sees it right away.
     */
    private void submitComplaint() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_complaint_title_required));
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError(getString(R.string.error_complaint_desc_required));
            etDescription.requestFocus();
            return;
        }

        int userId = sessionManager.getUserId();
        String category = categoryAdapter.getSelectedCategory();
        String priority = getSelectedPriority();

        long rowId = dbHelper.addComplaint(userId, title, category, priority, description);
        if (rowId != -1) {
            Toast.makeText(requireContext(), R.string.complaint_submitted_toast, Toast.LENGTH_SHORT).show();

            // Reset form fields
            etTitle.setText("");
            etDescription.setText("");
            rgPriority.check(R.id.rbPriorityMedium);
            categoryAdapter.setSelectedPosition(0);
            updateSelectedCategoryLabel();

            // Switch to My Complaints tab to show the new Pending ticket
            if (getActivity() instanceof UserDashboardActivity) {
                ((UserDashboardActivity) getActivity()).navigateToTab(R.id.nav_my_complaints);
            }
        }
    }
}
