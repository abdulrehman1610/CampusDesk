package com.example.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

/**
 * ComplaintDetailActivity shows all details of a student's complaint, including the admin reply
 * (or "No reply yet") (PRD U7). While the complaint is Pending, the student can Edit it
 * (launched via ActivityResultLauncher, PRD U8) or Delete it (with AlertDialog confirmation, PRD U9).
 */
public class ComplaintDetailActivity extends AppCompatActivity {

    private View btnBack;
    private TextView tvDate;
    private TextView tvTicketCode;
    private TextView tvStatusBadge;
    private TextView tvCategory;
    private TextView tvPriority;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvAdminReply;
    private View layoutPendingActions;
    private Button btnEdit;
    private Button btnDelete;
    private TextView tvResolvedLockNote;

    private DatabaseHelper dbHelper;
    private int complaintId = -1;
    private Complaint currentComplaint;

    // ActivityResultLauncher for EditComplaintActivity (PRD Section 3 & U8)
    private final ActivityResultLauncher<Intent> editComplaintLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Refresh details immediately and propagate RESULT_OK back to MyComplaintsFragment
                    loadComplaintDetails();
                    setResult(RESULT_OK);
                }
            }
    );

    /**
     * Initializes the detail screen, loads the complaint from SQLite, and attaches click listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        dbHelper = new DatabaseHelper(this);
        complaintId = getIntent().getIntExtra(Constants.EXTRA_COMPLAINT_ID, -1);
        if (complaintId == -1) {
            finish();
            return;
        }

        bindViews();
        loadComplaintDetails();
        setupListeners();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBackComplaintDetail);
        tvDate = findViewById(R.id.tvDetailComplaintDate);
        tvTicketCode = findViewById(R.id.tvDetailTicketCode);
        tvStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvPriority = findViewById(R.id.tvDetailPriority);
        tvTitle = findViewById(R.id.tvDetailComplaintTitle);
        tvDescription = findViewById(R.id.tvDetailComplaintDescription);
        tvAdminReply = findViewById(R.id.tvDetailAdminReply);
        layoutPendingActions = findViewById(R.id.layoutPendingActions);
        btnEdit = findViewById(R.id.btnEditComplaint);
        btnDelete = findViewById(R.id.btnDeleteComplaint);
        tvResolvedLockNote = findViewById(R.id.tvResolvedLockNote);
    }

    /**
     * Fetches the complaint by ID from SQLite and populates all UI fields and badges.
     */
    private void loadComplaintDetails() {
        currentComplaint = dbHelper.getComplaintById(complaintId);
        if (currentComplaint == null) {
            finish();
            return;
        }

        tvTicketCode.setText(currentComplaint.getTicketCode());
        tvDate.setText(getString(R.string.complaint_detail_filed_on_fmt, currentComplaint.getCreatedDate()));
        tvCategory.setText(currentComplaint.getCategory());
        tvPriority.setText(getString(R.string.complaint_card_priority_fmt, currentComplaint.getPriority()));
        tvTitle.setText(currentComplaint.getTitle());
        tvDescription.setText(currentComplaint.getDescription());

        // Admin reply or "No reply yet" (PRD U7)
        String reply = currentComplaint.getReply();
        if (reply != null && !reply.trim().isEmpty()) {
            tvAdminReply.setText(reply);
            tvAdminReply.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            tvAdminReply.setText(R.string.complaint_detail_no_reply);
            tvAdminReply.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }

        // Status badge and Edit/Delete lock check (PRD U8 & U9: edit/delete only while Pending)
        boolean isPending = Constants.STATUS_PENDING.equalsIgnoreCase(currentComplaint.getStatus());
        tvStatusBadge.setText(currentComplaint.getStatus());
        if (isPending) {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
            layoutPendingActions.setVisibility(View.VISIBLE);
            tvResolvedLockNote.setVisibility(View.GONE);
        } else {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_resolved);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_resolved));
            layoutPendingActions.setVisibility(View.GONE);
            tvResolvedLockNote.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Attaches click listeners for Back, Edit (via ActivityResultLauncher), and Delete (via AlertDialog).
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (currentComplaint == null) {
                return;
            }
            Intent intent = new Intent(ComplaintDetailActivity.this, EditComplaintActivity.class);
            intent.putExtra(Constants.EXTRA_COMPLAINT_ID, currentComplaint.getId());
            editComplaintLauncher.launch(intent);
        });

        btnDelete.setOnClickListener(v -> confirmAndDeleteComplaint());
    }

    /**
     * Shows a confirmation AlertDialog and deletes the complaint if confirmed (PRD U9).
     */
    private void confirmAndDeleteComplaint() {
        if (currentComplaint == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.complaint_delete_title)
                .setMessage(getString(R.string.complaint_delete_message, currentComplaint.getTicketCode()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteComplaint(currentComplaint.getId());
                    if (deleted) {
                        Toast.makeText(this, R.string.complaint_deleted_toast, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
