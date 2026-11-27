package com.example.myapplication.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Complaint;
import com.example.myapplication.utils.Constants;

/**
 * EditComplaintActivity allows a student to edit their complaint's title, category,
 * priority, and description while its status is still Pending (PRD Section 5.2 U8).
 * When saved, it sets RESULT_OK and finishes so the calling ActivityResultLauncher refreshes.
 */
public class EditComplaintActivity extends AppCompatActivity {

    private View btnBack;
    private EditText etTitle;
    private Spinner spinnerCategory;
    private RadioGroup rgPriority;
    private EditText etDescription;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private int complaintId = -1;

    /**
     * Initializes the edit form, populates current complaint values, and wires the Save button.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_complaint);

        dbHelper = new DatabaseHelper(this);
        complaintId = getIntent().getIntExtra(Constants.EXTRA_COMPLAINT_ID, -1);
        if (complaintId == -1) {
            finish();
            return;
        }

        bindViews();
        setupCategorySpinner();
        populateExistingData();
        setupListeners();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBackEditComplaint);
        etTitle = findViewById(R.id.etEditComplaintTitle);
        spinnerCategory = findViewById(R.id.spinnerEditCategory);
        rgPriority = findViewById(R.id.rgEditPriority);
        etDescription = findViewById(R.id.etEditComplaintDescription);
        btnSave = findViewById(R.id.btnSaveEditedComplaint);
    }

    /**
     * Populates the category Spinner with the 6 complaint categories from Constants.CATEGORIES.
     */
    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.CATEGORIES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    /**
     * Loads the existing complaint from SQLite and pre-fills the form fields.
     */
    private void populateExistingData() {
        Complaint complaint = dbHelper.getComplaintById(complaintId);
        if (complaint == null || !Constants.STATUS_PENDING.equalsIgnoreCase(complaint.getStatus())) {
            finish();
            return;
        }

        etTitle.setText(complaint.getTitle());
        etDescription.setText(complaint.getDescription());

        // Pre-select category in Spinner
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            if (Constants.CATEGORIES[i].equalsIgnoreCase(complaint.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Pre-select priority in RadioGroup
        if (Constants.PRIORITY_LOW.equalsIgnoreCase(complaint.getPriority())) {
            rgPriority.check(R.id.rbEditPriorityLow);
        } else if (Constants.PRIORITY_HIGH.equalsIgnoreCase(complaint.getPriority())) {
            rgPriority.check(R.id.rbEditPriorityHigh);
        } else {
            rgPriority.check(R.id.rbEditPriorityMedium);
        }
    }

    /**
     * Attaches click listeners for the Back and Save Changes buttons.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    /**
     * Validates inputs, updates the complaint in SQLite, returns RESULT_OK, and closes the screen.
     */
    private void saveChanges() {
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

        String category = spinnerCategory.getSelectedItem().toString();
        String priority = getSelectedPriority();

        boolean updated = dbHelper.updateComplaint(complaintId, title, category, priority, description);
        if (updated) {
            Toast.makeText(this, R.string.complaint_updated_toast, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Returns "Low", "Medium", or "High" according to the selected RadioButton.
     */
    private String getSelectedPriority() {
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbEditPriorityLow) {
            return Constants.PRIORITY_LOW;
        } else if (checkedId == R.id.rbEditPriorityHigh) {
            return Constants.PRIORITY_HIGH;
        } else {
            return Constants.PRIORITY_MEDIUM;
        }
    }
}
