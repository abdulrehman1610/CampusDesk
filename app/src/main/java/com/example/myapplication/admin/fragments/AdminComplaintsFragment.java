package com.example.myapplication.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ComplaintAdapter;
import com.example.myapplication.admin.AdminComplaintDetailActivity;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * AdminComplaintsFragment displays all student complaints in a RecyclerView (Figma 2:1398 & PRD D3).
 * It includes a Status Filter Spinner (All / Pending / Resolved, PRD D4) synced with filter chips,
 * a Sort Spinner (Date / Priority, PRD D9), a search bar, and opens AdminComplaintDetailActivity on tap.
 */
public class AdminComplaintsFragment extends Fragment implements ComplaintAdapter.OnComplaintClickListener {

    private TextView tvQueueCountBadge;
    private TextView tvStatPending;
    private TextView tvStatResolved;
    private TextView tvStatTotal;
    private EditText etSearch;
    private TextView chipAll;
    private TextView chipPending;
    private TextView chipResolved;
    private Spinner spinnerStatusFilter;
    private Spinner spinnerSort;
    private RecyclerView rvComplaints;
    private TextView tvEmpty;

    private DatabaseHelper dbHelper;
    private ComplaintAdapter complaintAdapter;
    private List<Complaint> allComplaints = new ArrayList<>();
    private int selectedStatusIndex = 0; // 0 = All, 1 = Pending, 2 = Resolved
    private int selectedSortIndex = 0;   // 0 = Sort by Date, 1 = Sort by Priority

    /**
     * Inflates fragment_admin_complaints.xml and initializes the RecyclerView, Spinners, and search.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_complaints, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        bindViews(view);
        setupRecyclerView();
        setupSpinners();
        setupListeners();

        return view;
    }

    /**
     * Reloads all complaints from SQLite whenever the admin returns to this tab.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadComplaints();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews(View view) {
        tvQueueCountBadge = view.findViewById(R.id.tvAdminQueueCountBadge);
        tvStatPending = view.findViewById(R.id.tvAdminStatPending);
        tvStatResolved = view.findViewById(R.id.tvAdminStatResolved);
        tvStatTotal = view.findViewById(R.id.tvAdminStatTotal);
        etSearch = view.findViewById(R.id.etSearchAdminComplaints);
        chipAll = view.findViewById(R.id.chipAdminAll);
        chipPending = view.findViewById(R.id.chipAdminPending);
        chipResolved = view.findViewById(R.id.chipAdminResolved);
        spinnerStatusFilter = view.findViewById(R.id.spinnerAdminStatusFilter);
        spinnerSort = view.findViewById(R.id.spinnerAdminSort);
        rvComplaints = view.findViewById(R.id.rvAdminComplaints);
        tvEmpty = view.findViewById(R.id.tvEmptyAdminComplaints);
    }

    /**
     * Configures the RecyclerView with ComplaintAdapter (showStudentName = true for Admin view).
     */
    private void setupRecyclerView() {
        rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        complaintAdapter = new ComplaintAdapter(new ArrayList<>(), true, this);
        rvComplaints.setAdapter(complaintAdapter);
    }

    /**
     * Configures the Status Filter Spinner (PRD D4) and the Sort Spinner (PRD D9).
     */
    private void setupSpinners() {
        // 1. Status Filter Spinner: All / Pending / Resolved (PRD D4)
        String[] statusOptions = {
                getString(R.string.admin_filter_all),
                getString(R.string.admin_filter_pending),
                getString(R.string.admin_filter_resolved)
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setStatusFilterIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 2. Sort Spinner: Sort by Date / Sort by Priority (PRD D9)
        String[] sortOptions = {
                getString(R.string.sort_by_date_newest),
                getString(R.string.sort_by_priority_high)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortIndex = position;
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Wires the search EditText and the 3 status filter chips (synced with the Status Spinner).
     */
    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipAll.setOnClickListener(v -> spinnerStatusFilter.setSelection(0));
        chipPending.setOnClickListener(v -> spinnerStatusFilter.setSelection(1));
        chipResolved.setOnClickListener(v -> spinnerStatusFilter.setSelection(2));
    }

    /**
     * Updates the active status filter index, syncs the visual chips, and refreshes the list.
     */
    private void setStatusFilterIndex(int index) {
        selectedStatusIndex = index;
        TextView[] chips = {chipAll, chipPending, chipResolved};
        for (int i = 0; i < chips.length; i++) {
            if (i == index) {
                chips[i].setBackgroundResource(R.drawable.bg_button_crimson);
                chips[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                chips[i].setBackgroundResource(R.drawable.bg_input_pill);
                chips[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        }
        applyFiltersAndSort();
    }

    /**
     * Loads complaints and summary counts from SQLite.
     */
    private void loadComplaints() {
        allComplaints = dbHelper.getAllComplaints();

        int total = dbHelper.getComplaintCount();
        int pending = dbHelper.getComplaintCountByStatus(Constants.STATUS_PENDING);
        int resolved = dbHelper.getComplaintCountByStatus(Constants.STATUS_RESOLVED);

        tvStatPending.setText(String.valueOf(pending));
        tvStatResolved.setText(String.valueOf(resolved));
        tvStatTotal.setText(String.valueOf(total));

        if (getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) getActivity()).updateHeaderSummary();
        }

        applyFiltersAndSort();
    }

    /**
     * Applies the status filter, search text filter, and sort order to the complaints list.
     */
    private void applyFiltersAndSort() {
        // Query by status via DatabaseHelper.getComplaintsByStatus when Pending or Resolved is chosen
        List<Complaint> sourceList;
        if (selectedStatusIndex == 1) {
            sourceList = dbHelper.getComplaintsByStatus(Constants.STATUS_PENDING);
        } else if (selectedStatusIndex == 2) {
            sourceList = dbHelper.getComplaintsByStatus(Constants.STATUS_RESOLVED);
        } else {
            sourceList = new ArrayList<>(allComplaints);
        }

        String query = etSearch.getText() != null
                ? etSearch.getText().toString().trim().toLowerCase(Locale.US)
                : "";

        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : sourceList) {
            String studentName = c.getUserName() != null ? c.getUserName().toLowerCase(Locale.US) : "";
            if (query.isEmpty()
                    || c.getTitle().toLowerCase(Locale.US).contains(query)
                    || c.getDescription().toLowerCase(Locale.US).contains(query)
                    || c.getCategory().toLowerCase(Locale.US).contains(query)
                    || c.getTicketCode().toLowerCase(Locale.US).contains(query)
                    || studentName.contains(query)) {
                filtered.add(c);
            }
        }

        if (selectedSortIndex == 1) {
            Collections.sort(filtered, (a, b) -> priorityRank(b.getPriority()) - priorityRank(a.getPriority()));
        }

        tvQueueCountBadge.setText(getString(R.string.my_complaints_tickets_count_fmt, filtered.size()));
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
     * Converts priority string to numeric rank (High=3, Medium=2, Low=1) for sorting.
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
     * Opens AdminComplaintDetailActivity when the admin taps any complaint card (PRD D5).
     */
    @Override
    public void onComplaintClick(Complaint complaint) {
        Intent intent = new Intent(requireContext(), AdminComplaintDetailActivity.class);
        intent.putExtra(Constants.EXTRA_COMPLAINT_ID, complaint.getId());
        startActivity(intent);
    }
}
