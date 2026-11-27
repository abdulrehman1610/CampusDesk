package com.example.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Notice;
import com.example.myapplication.utils.Constants;

/**
 * AddEditNoticeActivity handles both adding a new notice and editing an existing notice (PRD D7).
 * The mode ("add" or "edit") and optional notice ID are passed via Intent extras.
 */
public class AddEditNoticeActivity extends AppCompatActivity {

    private View btnBack;
    private TextView tvFormTitle;
    private EditText etTitle;
    private EditText etContent;
    private Button btnSave;
    private Button btnDelete;

    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private int noticeId = -1;

    /**
     * Initializes the form views, checks the Intent extras for add vs edit mode, and attaches listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_notice);

        dbHelper = new DatabaseHelper(this);

        bindViews();
        configureModeFromIntent();
        setupListeners();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBackNoticeForm);
        tvFormTitle = findViewById(R.id.tvNoticeFormTitle);
        etTitle = findViewById(R.id.etNoticeTitle);
        etContent = findViewById(R.id.etNoticeContent);
        btnSave = findViewById(R.id.btnSaveNotice);
        btnDelete = findViewById(R.id.btnDeleteNoticeForm);
    }

    /**
     * Reads EXTRA_MODE and EXTRA_NOTICE_ID from the starting Intent and pre-fills data in edit mode.
     */
    private void configureModeFromIntent() {
        Intent intent = getIntent();
        String mode = intent.getStringExtra(Constants.EXTRA_MODE);
        noticeId = intent.getIntExtra(Constants.EXTRA_NOTICE_ID, -1);

        isEditMode = Constants.MODE_EDIT.equals(mode) && noticeId != -1;

        if (isEditMode) {
            tvFormTitle.setText(R.string.notice_edit_title);
            btnSave.setText(R.string.notice_btn_update);
            btnDelete.setVisibility(View.VISIBLE);

            Notice existing = dbHelper.getNoticeById(noticeId);
            if (existing != null) {
                etTitle.setText(existing.getTitle());
                etContent.setText(existing.getContent());
            }
        } else {
            tvFormTitle.setText(R.string.notice_add_title);
            btnSave.setText(R.string.notice_btn_publish);
            btnDelete.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up click listeners for Back, Save/Publish, and Delete buttons.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNotice());
        btnDelete.setOnClickListener(v -> confirmAndDeleteNotice());
    }

    /**
     * Validates the title and content fields and inserts or updates the notice in SQLite.
     */
    private void saveNotice() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_notice_title_required));
            etTitle.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            etContent.setError(getString(R.string.error_notice_content_required));
            etContent.requestFocus();
            return;
        }

        if (isEditMode) {
            boolean updated = dbHelper.updateNotice(noticeId, title, content);
            if (updated) {
                Toast.makeText(this, R.string.notice_updated_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            long newId = dbHelper.addNotice(title, content);
            if (newId != -1) {
                Toast.makeText(this, R.string.notice_added_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Asks for confirmation via AlertDialog before deleting the notice in edit mode.
     */
    private void confirmAndDeleteNotice() {
        String title = etTitle.getText().toString().trim();
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_notice_delete_title)
                .setMessage(getString(R.string.admin_notice_delete_message, title))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteNotice(noticeId);
                    if (deleted) {
                        Toast.makeText(this, R.string.admin_notice_deleted_toast, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
