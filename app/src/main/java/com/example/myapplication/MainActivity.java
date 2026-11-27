package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.Notice;
import com.example.myapplication.models.User;

import java.util.List;

/**
 * MainActivity is the launcher activity.
 * In Milestone M1 it runs a quick database verification test to confirm that
 * SQLite tables, seeded admin, and seeded notices are created and readable.
 * In Milestone M2 this will become the session router to Login or Dashboards.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "M1_DB_TEST";

    /**
     * Initializes the screen and runs the Milestone M1 database self-test.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        runMilestone1DatabaseTest();
    }

    /**
     * Temporary Milestone M1 verification test: checks seeded admin login,
     * inserts and deletes a test notice, and shows the counts via Logcat and Toast.
     */
    private void runMilestone1DatabaseTest() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // 1. Verify seeded admin can be read
        User admin = dbHelper.loginUser("admin@uni.com", "admin123");
        String adminStatus = (admin != null) ? ("Admin OK (" + admin.getRole() + ")") : "Admin MISSING";

        // 2. Verify insert + delete + read on notices table
        long tempId = dbHelper.addNotice("M1 Test Notice", "Temporary test row to verify insert.");
        boolean deleted = dbHelper.deleteNotice((int) tempId);

        List<Notice> notices = dbHelper.getAllNotices();
        int userCount = dbHelper.getUserCount();

        String message = "DB Ready: " + adminStatus
                + " | Users=" + userCount
                + " | Seeded Notices=" + notices.size()
                + " | Insert/Delete=" + (tempId != -1 && deleted);

        Log.d(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
