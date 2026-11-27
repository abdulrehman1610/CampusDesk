package com.example.myapplication.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.AdminPagerAdapter;
import com.example.myapplication.adapters.ComplaintAdapter;
import com.example.myapplication.admin.AddEditNoticeActivity;
import com.example.myapplication.admin.AdminComplaintDetailActivity;
import com.example.myapplication.admin.AdminDashboardActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OverviewFragment displays the Admin Portal statistics dashboard (Figma 2:1160 & PRD Section 5.3 D2).
 * It runs SQL COUNT(*) queries for total complaints, pending, resolved, notices, and users,
 * updates the Resolution Pace progress bar, and lists the 3 most recent student filings.
 */
public class OverviewFragment extends Fragment implements ComplaintAdapter.OnComplaintClickListener {

    private View cardPending;
    private View cardResolved;
    private View cardNotices;
    private View cardUsers;
    private TextView tvPendingCount;
    private TextView tvTotalSub;
    private TextView tvResolvedCount;
    private TextView tvResolvedRateTag;
    private TextView tvNoticesCount;
    private TextView tvUsersCount;
    private TextView tvPaceSummary;
    private ProgressBar progressResolutionRate;
    private TextView tvResolvedPct;
    private TextView tvPendingPct;
    private View cardQueueAttention;
    private TextView tvQueueSub;
    private Button btnPostNotice;
    private Button btnViewQueue;
    private TextView tvViewAll;
    private RecyclerView rvRecentComplaints;
    private TextView tvRecentEmpty;

    private DatabaseHelper dbHelper;
    private ComplaintAdapter recentAdapter;

    /**
     * Inflates fragment_overview.xml, initializes the recent filings RecyclerView, and attaches listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        bindViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    /**
     * Reloads all statistics and recent filings from SQLite every time the Overview tab is shown.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadOverviewData();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews(View view) {
        cardPending = view.findViewById(R.id.cardMetricPending);
        cardResolved = view.findViewById(R.id.cardMetricResolved);
        cardNotices = view.findViewById(R.id.cardMetricNotices);
        cardUsers = view.findViewById(R.id.cardMetricUsers);
        tvPendingCount = view.findViewById(R.id.tvMetricPendingCount);
        tvTotalSub = view.findViewById(R.id.tvMetricTotalSub);
        tvResolvedCount = view.findViewById(R.id.tvMetricResolvedCount);
        tvResolvedRateTag = view.findViewById(R.id.tvMetricResolvedRateTag);
        tvNoticesCount = view.findViewById(R.id.tvMetricNoticesCount);
        tvUsersCount = view.findViewById(R.id.tvMetricUsersCount);
        tvPaceSummary = view.findViewById(R.id.tvOverviewPaceSummary);
        progressResolutionRate = view.findViewById(R.id.progressResolutionRate);
        tvResolvedPct = view.findViewById(R.id.tvOverviewResolvedPct);
        tvPendingPct = view.findViewById(R.id.tvOverviewPendingPct);
        cardQueueAttention = view.findViewById(R.id.cardQueueAttention);
        tvQueueSub = view.findViewById(R.id.tvOverviewQueueSub);
        btnPostNotice = view.findViewById(R.id.btnOverviewPostNotice);
        btnViewQueue = view.findViewById(R.id.btnOverviewViewQueue);
        tvViewAll = view.findViewById(R.id.tvOverviewViewAll);
        rvRecentComplaints = view.findViewById(R.id.rvOverviewRecentComplaints);
        tvRecentEmpty = view.findViewById(R.id.tvOverviewRecentEmpty);
    }

    /**
     * Configures the Recent Filings RecyclerView using ComplaintAdapter with student names visible.
     */
    private void setupRecyclerView() {
        rvRecentComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentAdapter = new ComplaintAdapter(new ArrayList<>(), true, this);
        rvRecentComplaints.setAdapter(recentAdapter);
    }

    /**
     * Attaches click listeners to the metric cards and action shortcut buttons.
     */
    private void setupListeners() {
        cardPending.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_COMPLAINTS));
        cardResolved.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_COMPLAINTS));
        cardNotices.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_NOTICES));
        cardUsers.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_USERS));
        cardQueueAttention.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_COMPLAINTS));
        btnViewQueue.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_COMPLAINTS));
        tvViewAll.setOnClickListener(v -> switchAdminTab(AdminPagerAdapter.TAB_COMPLAINTS));

        btnPostNotice.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditNoticeActivity.class);
            intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_ADD);
            startActivity(intent);
        });
    }

    /**
     * Helper method that asks AdminDashboardActivity to switch to another tab.
     */
    private void switchAdminTab(int tabIndex) {
        if (getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) getActivity()).switchToTab(tabIndex);
        }
    }

    /**
     * Queries SQLite COUNT(*) methods for all 5 metrics and loads the latest 3 complaints (PRD D2).
     */
    private void loadOverviewData() {
        int totalComplaints = dbHelper.getComplaintCount();
        int pendingCount = dbHelper.getComplaintCountByStatus(Constants.STATUS_PENDING);
        int resolvedCount = dbHelper.getComplaintCountByStatus(Constants.STATUS_RESOLVED);
        int noticesCount = dbHelper.getNoticeCount();
        int usersCount = dbHelper.getUserCount();

        int resolvedRate = totalComplaints > 0 ? Math.round((resolvedCount * 100f) / totalComplaints) : 100;
        int pendingRate = totalComplaints > 0 ? (100 - resolvedRate) : 0;

        tvPendingCount.setText(String.format(Locale.US, "%02d", pendingCount));
        tvTotalSub.setText(getString(R.string.overview_sub_pending_fmt, totalComplaints));
        tvResolvedCount.setText(String.format(Locale.US, "%02d", resolvedCount));
        tvResolvedRateTag.setText(getString(R.string.overview_tag_rate_fmt, resolvedRate));
        tvNoticesCount.setText(String.format(Locale.US, "%02d", noticesCount));
        tvUsersCount.setText(String.format(Locale.US, "%02d", usersCount));

        tvPaceSummary.setText(getString(R.string.overview_pace_body_fmt, resolvedCount, totalComplaints));
        progressResolutionRate.setProgress(resolvedRate);
        tvResolvedPct.setText(getString(R.string.overview_pace_resolved_fmt, resolvedRate));
        tvPendingPct.setText(getString(R.string.overview_pace_pending_fmt, pendingRate));

        tvQueueSub.setText(getString(R.string.overview_queue_body_fmt, pendingCount));
        tvViewAll.setText(getString(R.string.overview_view_all_fmt, totalComplaints));

        // Update the top header micro-bar on AdminDashboardActivity as well
        if (getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) getActivity()).updateHeaderSummary();
        }

        // Load latest 3 complaints for Recent Filings preview
        List<Complaint> recentList = dbHelper.getRecentComplaints(3);
        recentAdapter.updateList(recentList);

        if (recentList.isEmpty()) {
            rvRecentComplaints.setVisibility(View.GONE);
            tvRecentEmpty.setVisibility(View.VISIBLE);
        } else {
            rvRecentComplaints.setVisibility(View.VISIBLE);
            tvRecentEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Opens AdminComplaintDetailActivity when the admin taps a complaint in Recent Filings.
     */
    @Override
    public void onComplaintClick(Complaint complaint) {
        Intent intent = new Intent(requireContext(), AdminComplaintDetailActivity.class);
        intent.putExtra(Constants.EXTRA_COMPLAINT_ID, complaint.getId());
        startActivity(intent);
    }
}
