package com.example.myapplication.user.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ComplaintAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.user.ComplaintDetailActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * MyComplaintsFragment displays only the logged-in student's complaints (Figma 2:698 & PRD U6).
 * It populates real SQL counts (Total, Pending, Resolved), supports search and category filtering,
 * sorts tickets via a Spinner, opens ComplaintDetailActivity via an ActivityResultLauncher,
 * and dials the campus help desk via an implicit ACTION_DIAL Intent.
 */
public class MyComplaintsFragment extends Fragment implements ComplaintAdapter.OnComplaintClickListener {

    private TextView tvActiveBadge;
    private TextView tvStatTotal;
    private TextView tvStatPending;
    private TextView tvStatResolved;
    private EditText etSearch;
    private TextView chipAll;
    private TextView chipHostel;
    private TextView chipAcademic;
    private TextView chipTransport;
    private TextView chipFees;
    private TextView chipLibrary;
    private TextView chipGeneral;
    private TextView tvTicketCountBadge;
    private Spinner spinnerSort;
    private RecyclerView rvComplaints;
    private TextView tvEmpty;
    private Button btnCallDesk;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ComplaintAdapter complaintAdapter;
    private List<Complaint> userComplaints = new ArrayList<>();
    private String selectedCategoryFilter = "";
    private int selectedSortIndex = 0;

    // ActivityResultLauncher that refreshes the list when returning from detail/edit with RESULT_OK
    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadComplaintsAndStats();
                }
            }
    );

    /**
     * Inflates fragment_my_complaints.xml and initializes the RecyclerView, Spinner, and filters.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_complaints, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        bindViews(view);
        setupRecyclerView();
        setupSortSpinner();
        setupListeners();

        return view;
    }

    /**
     * Refreshes the student's complaints and stat cards every time the screen resumes.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadComplaintsAndStats();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews(View view) {
        tvActiveBadge = view.findViewById(R.id.tvActiveComplaintsBadge);
        tvStatTotal = view.findViewById(R.id.tvUserStatTotal);
        tvStatPending = view.findViewById(R.id.tvUserStatPending);
        tvStatResolved = view.findViewById(R.id.tvUserStatResolved);
        etSearch = view.findViewById(R.id.etSearchMyComplaints);
        chipAll = view.findViewById(R.id.chipCatAll);
        chipHostel = view.findViewById(R.id.chipCatHostel);
        chipAcademic = view.findViewById(R.id.chipCatAcademic);
        chipTransport = view.findViewById(R.id.chipCatTransport);
        chipFees = view.findViewById(R.id.chipCatFees);
        chipLibrary = view.findViewById(R.id.chipCatLibrary);
        chipGeneral = view.findViewById(R.id.chipCatGeneral);
        tvTicketCountBadge = view.findViewById(R.id.tvTicketCountBadge);
        spinnerSort = view.findViewById(R.id.spinnerSortMyComplaints);
        rvComplaints = view.findViewById(R.id.rvMyComplaints);
        tvEmpty = view.findViewById(R.id.tvEmptyMyComplaints);
        btnCallDesk = view.findViewById(R.id.btnCallDesk);
    }

    /**
     * Configures the RecyclerView with a vertical LinearLayoutManager and ComplaintAdapter.
     */
    private void setupRecyclerView() {
        rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        complaintAdapter = new ComplaintAdapter(new ArrayList<>(), false, this);
        rvComplaints.setAdapter(complaintAdapter);
    }

    /**
     * Populates the Sort Spinner with 3 sorting options (Date, Priority, Pending First).
     */
    private void setupSortSpinner() {
        String[] sortOptions = {
                getString(R.string.sort_by_date_newest),
                getString(R.string.sort_by_priority_high),
                getString(R.string.sort_by_pending_first)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortIndex = position;
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Attaches search TextWatcher, category chip click listeners, and the Call Desk implicit Intent.
     */
    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipAll.setOnClickListener(v -> selectCategoryChip("", chipAll));
        chipHostel.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_HOSTEL, chipHostel));
        chipAcademic.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_ACADEMIC, chipAcademic));
        chipTransport.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_TRANSPORT, chipTransport));
        chipFees.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_FEES, chipFees));
        chipLibrary.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_LIBRARY, chipLibrary));
        chipGeneral.setOnClickListener(v -> selectCategoryChip(Constants.CATEGORY_GENERAL, chipGeneral));

        // Call Desk button fires an implicit ACTION_DIAL Intent
        btnCallDesk.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(Constants.HELP_DESK_PHONE));
            startActivity(dialIntent);
        });
    }

    /**
     * Highlights the tapped category filter chip and re-filters the complaints list.
     */
    private void selectCategoryChip(String category, TextView activeChip) {
        selectedCategoryFilter = category;
        TextView[] allChips = {chipAll, chipHostel, chipAcademic, chipTransport, chipFees, chipLibrary, chipGeneral};

        for (TextView chip : allChips) {
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.bg_button_crimson);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_input_pill);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        }
        applyFilterAndSort();
    }

    /**
     * Queries SQLite for the logged-in student's complaints and stat counts.
     */
    private void loadComplaintsAndStats() {
        int userId = sessionManager.getUserId();
        userComplaints = dbHelper.getComplaintsByUser(userId);

        int total = dbHelper.getComplaintCountByUser(userId);
        int pending = dbHelper.getComplaintCountByUserAndStatus(userId, Constants.STATUS_PENDING);
        int resolved = dbHelper.getComplaintCountByUserAndStatus(userId, Constants.STATUS_RESOLVED);

        tvActiveBadge.setText(getString(R.string.my_complaints_active_fmt, pending));
        tvStatTotal.setText(String.format(Locale.US, "%02d", total));
        tvStatPending.setText(String.format(Locale.US, "%02d", pending));
        tvStatResolved.setText(String.format(Locale.US, "%02d", resolved));

        applyFilterAndSort();
    }

    /**
     * Filters the student's complaints by search query and category, sorts them, and updates the UI.
     */
    private void applyFilterAndSort() {
        String query = etSearch.getText() != null
                ? etSearch.getText().toString().trim().toLowerCase(Locale.US)
                : "";

        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : userComplaints) {
            boolean matchesCategory = selectedCategoryFilter.isEmpty()
                    || selectedCategoryFilter.equalsIgnoreCase(c.getCategory());
            boolean matchesSearch = query.isEmpty()
                    || c.getTitle().toLowerCase(Locale.US).contains(query)
                    || c.getDescription().toLowerCase(Locale.US).contains(query)
                    || c.getTicketCode().toLowerCase(Locale.US).contains(query);

            if (matchesCategory && matchesSearch) {
                filtered.add(c);
            }
        }

        // Apply selected Spinner sort order
        if (selectedSortIndex == 1) {
            // Sort by Priority: High -> Medium -> Low
            Collections.sort(filtered, (a, b) -> priorityRank(b.getPriority()) - priorityRank(a.getPriority()));
        } else if (selectedSortIndex == 2) {
            // Sort by Status: Pending first
            Collections.sort(filtered, (a, b) -> {
                boolean aPending = Constants.STATUS_PENDING.equalsIgnoreCase(a.getStatus());
                boolean bPending = Constants.STATUS_PENDING.equalsIgnoreCase(b.getStatus());
                return Boolean.compare(bPending, aPending);
            });
        }

        tvTicketCountBadge.setText(getString(R.string.my_complaints_tickets_count_fmt, filtered.size()));
        complaintAdapter.updateList(filtered);

        if (filtered.isEmpty()) {
            rvComplaints.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvComplaints.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Helper method that converts a priority name to a number (High=3, Medium=2, Low=1) for sorting.
     */
    private int priorityRank(String priority) {
        if (Constants.PRIORITY_HIGH.equalsIgnoreCase(priority)) {
            return 3;
        } else if (Constants.PRIORITY_MEDIUM.equalsIgnoreCase(priority)) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Opens ComplaintDetailActivity when a complaint card is tapped (PRD U7).
     */
    @Override
    public void onComplaintClick(Complaint complaint) {
        Intent intent = new Intent(requireContext(), ComplaintDetailActivity.class);
        intent.putExtra(Constants.EXTRA_COMPLAINT_ID, complaint.getId());
        detailLauncher.launch(intent);
    }
}
