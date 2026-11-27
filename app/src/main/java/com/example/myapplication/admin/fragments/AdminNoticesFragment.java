package com.example.myapplication.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.NoticeAdapter;
import com.example.myapplication.admin.AddEditNoticeActivity;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Notice;
import com.example.myapplication.utils.Constants;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AdminNoticesFragment displays all official notices in a RecyclerView (PRD Section 5.3 D7).
 * The admin can search notices, tap the "+ New Notice" FloatingActionButton to add a notice,
 * tap Edit on any card to modify it, or tap Delete to remove it after an AlertDialog confirmation.
 */
public class AdminNoticesFragment extends Fragment implements NoticeAdapter.OnNoticeActionListener {

    private EditText etSearch;
    private TextView tvCountBadge;
    private RecyclerView rvNotices;
    private TextView tvEmpty;
    private ExtendedFloatingActionButton fabAddNotice;

    private DatabaseHelper dbHelper;
    private NoticeAdapter noticeAdapter;
    private List<Notice> allNotices = new ArrayList<>();

    /**
     * Inflates fragment_admin_notices.xml and initializes the RecyclerView, search bar, and FAB.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_notices, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        etSearch = view.findViewById(R.id.etSearchAdminNotices);
        tvCountBadge = view.findViewById(R.id.tvAdminNoticeCountBadge);
        rvNotices = view.findViewById(R.id.rvAdminNotices);
        tvEmpty = view.findViewById(R.id.tvEmptyAdminNotices);
        fabAddNotice = view.findViewById(R.id.fabAddNotice);

        rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        noticeAdapter = new NoticeAdapter(new ArrayList<>(), true, this);
        rvNotices.setAdapter(noticeAdapter);

        setupListeners();
        return view;
    }

    /**
     * Reloads the notices list whenever the admin returns to this tab or closes AddEditNoticeActivity.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadNotices();
    }

    /**
     * Attaches the "+ New Notice" FAB click listener and live search TextWatcher.
     */
    private void setupListeners() {
        fabAddNotice.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditNoticeActivity.class);
            intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_ADD);
            startActivity(intent);
        });

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
     * Reads all notices from SQLite, updates the count badge, and applies any active search text.
     */
    private void loadNotices() {
        allNotices = dbHelper.getAllNotices();
        tvCountBadge.setText(String.valueOf(allNotices.size()));
        String query = etSearch.getText() != null ? etSearch.getText().toString() : "";
        filterNotices(query);
    }

    /**
     * Filters the notices list by title, content, or notice code and updates the empty-state visibility.
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
     * Opens AddEditNoticeActivity in edit mode when the admin taps a notice card.
     */
    @Override
    public void onNoticeClick(Notice notice) {
        openEditScreen(notice);
    }

    /**
     * Opens AddEditNoticeActivity in edit mode when the admin taps the pencil icon on a card.
     */
    @Override
    public void onEditClick(Notice notice) {
        openEditScreen(notice);
    }

    /**
     * Shows a confirmation AlertDialog and deletes the notice from SQLite when confirmed.
     */
    @Override
    public void onDeleteClick(Notice notice) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.admin_notice_delete_title)
                .setMessage(getString(R.string.admin_notice_delete_message, notice.getTitle()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteNotice(notice.getId());
                    if (deleted) {
                        Toast.makeText(requireContext(), R.string.admin_notice_deleted_toast, Toast.LENGTH_SHORT).show();
                        loadNotices();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /**
     * Helper method that starts AddEditNoticeActivity with MODE_EDIT and the notice ID extra.
     */
    private void openEditScreen(Notice notice) {
        Intent intent = new Intent(requireContext(), AddEditNoticeActivity.class);
        intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_EDIT);
        intent.putExtra(Constants.EXTRA_NOTICE_ID, notice.getId());
        startActivity(intent);
    }
}
