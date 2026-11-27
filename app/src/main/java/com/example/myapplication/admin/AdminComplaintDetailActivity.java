package com.example.myapplication.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

/**
 * AdminComplaintDetailActivity allows the administrator to inspect a student's complaint,
 * write or update an official reply, mark the status as Resolved (or back to Pending),
 * or delete the complaint with an AlertDialog confirmation (PRD Section 5.3 D5 & D6).
 */
public class AdminComplaintDetailActivity extends AppCompatActivity {

    private View btnBack;
    private TextView tvStudentAndDate;
    private TextView tvTicketCode;
    private TextView tvStatusBadge;
    private TextView tvCategory;
    private TextView tvPriority;
    private TextView tvTitle;
    private TextView tvDescription;
    private EditText etAdminReply;
    private Button btnMarkResolved;
    private Button btnMarkPending;
    private Button btnDelete;

    private DatabaseHelper dbHelper;
    private int complaintId = -1;
    private Complaint currentComplaint;

    /**
     * Initializes the admin detail views, loads the complaint by ID, and attaches button listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaint_detail);

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
        btnBack = findViewById(R.id.btnBackAdminComplaintDetail);
        tvStudentAndDate = findViewById(R.id.tvAdminDetailStudentAndDate);
        tvTicketCode = findViewById(R.id.tvAdminDetailTicketCode);
        tvStatusBadge = findViewById(R.id.tvAdminDetailStatusBadge);
        tvCategory = findViewById(R.id.tvAdminDetailCategory);
        tvPriority = findViewById(R.id.tvAdminDetailPriority);
        tvTitle = findViewById(R.id.tvAdminDetailTitle);
        tvDescription = findViewById(R.id.tvAdminDetailDescription);
        etAdminReply = findViewById(R.id.etAdminReply);
        btnMarkResolved = findViewById(R.id.btnMarkResolved);
        btnMarkPending = findViewById(R.id.btnMarkPending);
        btnDelete = findViewById(R.id.btnAdminDeleteComplaint);
    }

    /**
     * Fetches the complaint (joined with student name) from SQLite and populates all fields.
     */
    private void loadComplaintDetails() {
        currentComplaint = dbHelper.getComplaintById(complaintId);
        if (currentComplaint == null) {
            finish();
            return;
        }

        String studentName = (currentComplaint.getUserName() != null && !currentComplaint.getUserName().isEmpty())
                ? currentComplaint.getUserName()
                : "Student";

        tvStudentAndDate.setText(getString(R.string.complaint_card_student_fmt,
                studentName, currentComplaint.getCreatedDate()));
        tvTicketCode.setText(currentComplaint.getTicketCode());
        tvCategory.setText(currentComplaint.getCategory());
        tvPriority.setText(getString(R.string.complaint_card_priority_fmt, currentComplaint.getPriority()));
        tvTitle.setText(currentComplaint.getTitle());
        tvDescription.setText(currentComplaint.getDescription());

        if (currentComplaint.getReply() != null) {
            etAdminReply.setText(currentComplaint.getReply());
        }

        boolean isResolved = Constants.STATUS_RESOLVED.equalsIgnoreCase(currentComplaint.getStatus());
        tvStatusBadge.setText(currentComplaint.getStatus());
        if (isResolved) {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_resolved);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_resolved));
        } else {
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
        }
    }

    /**
     * Sets up click listeners for Back, Mark Resolved, Keep/Mark Pending, and Delete.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMarkResolved.setOnClickListener(v -> saveReplyAndStatus(Constants.STATUS_RESOLVED));
        btnMarkPending.setOnClickListener(v -> saveReplyAndStatus(Constants.STATUS_PENDING));
        btnDelete.setOnClickListener(v -> confirmAndDeleteComplaint());
    }

    /**
     * Saves the admin's reply and sets the complaint status to "Resolved" or "Pending" (PRD D5).
     */
    private void saveReplyAndStatus(String newStatus) {
        String reply = etAdminReply.getText().toString().trim();

        // Require a reply when resolving a complaint so the student knows what was done
        if (Constants.STATUS_RESOLVED.equals(newStatus) && reply.isEmpty()) {
            etAdminReply.setError(getString(R.string.error_admin_reply_required));
            etAdminReply.requestFocus();
            return;
        }

        boolean updated = dbHelper.replyToComplaint(complaintId, reply, newStatus);
        if (updated) {
            int toastRes = Constants.STATUS_RESOLVED.equals(newStatus)
                    ? R.string.admin_reply_saved_resolved_toast
                    : R.string.admin_reply_saved_pending_toast;
            Toast.makeText(this, toastRes, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Asks for confirmation with an AlertDialog before deleting the complaint (PRD D6).
     */
    private void confirmAndDeleteComplaint() {
        if (currentComplaint == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.complaint_delete_title)
                .setMessage(getString(R.string.complaint_delete_message, currentComplaint.getTicketCode()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteComplaint(complaintId);
                    if (deleted) {
                        Toast.makeText(this, R.string.complaint_deleted_toast, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
