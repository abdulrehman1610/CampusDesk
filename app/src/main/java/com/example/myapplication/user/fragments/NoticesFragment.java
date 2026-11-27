package com.example.myapplication.user.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.NoticeAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Notice;
import com.example.myapplication.user.NoticeDetailActivity;
import com.example.myapplication.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * NoticesFragment shows the student Notices Feed (Figma 2:467 & PRD U1, U2, U4, U11).
 * It features an Urgent Broadcast banner for the newest notice, a live search filter bar,
 * and a RecyclerView of all official campus notices (newest first).
 */
public class NoticesFragment extends Fragment implements NoticeAdapter.OnNoticeActionListener {

    private View layoutUrgentBanner;
    private TextView tvUrgentNoticeTitle;
    private EditText etSearch;
    private RecyclerView rvNotices;
    private TextView tvEmpty;

    private DatabaseHelper dbHelper;
    private NoticeAdapter noticeAdapter;
    private List<Notice> allNotices = new ArrayList<>();

    /**
     * Inflates fragment_notices.xml, sets up the RecyclerView in student mode, and wires search.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notices, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        layoutUrgentBanner = view.findViewById(R.id.layoutUrgentBanner);
        tvUrgentNoticeTitle = view.findViewById(R.id.tvUrgentNoticeTitle);
        etSearch = view.findViewById(R.id.etSearchUserNotices);
        rvNotices = view.findViewById(R.id.rvUserNotices);
        tvEmpty = view.findViewById(R.id.tvEmptyUserNotices);

        rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        // isAdmin = false hides the Edit and Delete buttons on each notice card
        noticeAdapter = new NoticeAdapter(new ArrayList<>(), false, this);
        rvNotices.setAdapter(noticeAdapter);

        setupSearchListener();
        return view;
    }

    /**
     * Refreshes the notices list from SQLite every time the fragment becomes visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadNotices();
    }

    /**
     * Filters notices live as the student types in the search bar (PRD U4).
     */
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Loads all notices from SQLite (newest first), updates the Urgent banner, and applies search.
     */
    private void loadNotices() {
        allNotices = dbHelper.getAllNotices();

        if (!allNotices.isEmpty()) {
            Notice newest = allNotices.get(0);
            layoutUrgentBanner.setVisibility(View.VISIBLE);
            tvUrgentNoticeTitle.setText(newest.getTitle());
            layoutUrgentBanner.setOnClickListener(v -> openNoticeDetail(newest));
        } else {
            layoutUrgentBanner.setVisibility(View.GONE);
        }

        String query = etSearch.getText() != null ? etSearch.getText().toString() : "";
        filterNotices(query);
    }

    /**
     * Filters the notices list by title or content and toggles the empty-state TextView (PRD U11).
     */
    private void filterNotices(String query) {
        String cleanQuery = query.trim().toLowerCase(Locale.US);
        List<Notice> filtered = new ArrayList<>();

        for (Notice notice : allNotices) {
            if (cleanQuery.isEmpty()
                    || notice.getTitle().toLowerCase(Locale.US).contains(cleanQuery)
                    || notice.getContent().toLowerCase(Locale.US).contains(cleanQuery)
                    || notice.getNoticeCode().toLowerCase(Locale.US).contains(cleanQuery)) {
                filtered.add(notice);
            }
        }

        noticeAdapter.updateList(filtered);

        if (filtered.isEmpty()) {
            rvNotices.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvNotices.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Opens NoticeDetailActivity via an explicit Intent when the student taps a notice card (PRD U2).
     */
    @Override
    public void onNoticeClick(Notice notice) {
        openNoticeDetail(notice);
    }

    /**
     * Unused in student mode (edit icon is hidden).
     */
    @Override
    public void onEditClick(Notice notice) {
    }

    /**
     * Unused in student mode (delete icon is hidden).
     */
    @Override
    public void onDeleteClick(Notice notice) {
    }

    /**
     * Launches NoticeDetailActivity passing the selected notice ID as an Intent extra.
     */
    private void openNoticeDetail(Notice notice) {
        Intent intent = new Intent(requireContext(), NoticeDetailActivity.class);
        intent.putExtra(Constants.EXTRA_NOTICE_ID, notice.getId());
        startActivity(intent);
    }
}
