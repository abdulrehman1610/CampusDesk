package com.example.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Notice;
import com.example.myapplication.utils.Constants;

/**
 * NoticeDetailActivity displays the full details of a single campus notice (PRD U2)
 * and provides a "Share Notice" button that uses an implicit ACTION_SEND Intent (PRD U3).
 */
public class NoticeDetailActivity extends AppCompatActivity {

    private View btnBack;
    private TextView tvDate;
    private TextView tvCode;
    private TextView tvTitle;
    private TextView tvContent;
    private Button btnShare;

    private Notice currentNotice;

    /**
     * Initializes the detail views, loads the notice by ID from SQLite, and wires the Share button.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        bindViews();
        loadNoticeFromIntent();
        setupListeners();
    }

    /**
     * Connects Java fields to XML views using findViewById.
     */
    private void bindViews() {
        btnBack = findViewById(R.id.btnBackNoticeDetail);
        tvDate = findViewById(R.id.tvDetailNoticeDate);
        tvCode = findViewById(R.id.tvDetailNoticeCode);
        tvTitle = findViewById(R.id.tvDetailNoticeTitle);
        tvContent = findViewById(R.id.tvDetailNoticeContent);
        btnShare = findViewById(R.id.btnShareNotice);
    }

    /**
     * Reads EXTRA_NOTICE_ID from the starting Intent and fetches the Notice from SQLite.
     */
    private void loadNoticeFromIntent() {
        int noticeId = getIntent().getIntExtra(Constants.EXTRA_NOTICE_ID, -1);
        if (noticeId == -1) {
            finish();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        currentNotice = dbHelper.getNoticeById(noticeId);
        if (currentNotice == null) {
            finish();
            return;
        }

        tvCode.setText(currentNotice.getNoticeCode());
        tvDate.setText(getString(R.string.notice_detail_published_prefix, currentNotice.getCreatedDate()));
        tvTitle.setText(currentNotice.getTitle());
        tvContent.setText(currentNotice.getContent());
    }

    /**
     * Sets up the Back button and the implicit Share intent button.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareNotice());
    }

    /**
     * Shares the notice title and content with other apps using an implicit ACTION_SEND Intent (PRD U3).
     */
    private void shareNotice() {
        if (currentNotice == null) {
            return;
        }

        String shareBody = getString(
                R.string.notice_share_template,
                currentNotice.getTitle(),
                currentNotice.getNoticeCode(),
                currentNotice.getCreatedDate(),
                currentNotice.getContent()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentNotice.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.notice_share_chooser)));
    }
}
