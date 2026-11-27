package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.models.Complaint;
import com.example.myapplication.models.Notice;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DatabaseHelper manages the local SQLite database ("complaint_app.db").
 * It creates the tables, inserts initial seed data on first install, and provides
 * CRUD (Create, Read, Update, Delete) and statistics methods for Users, Complaints, and Notices.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database file name and version number (PRD Section 6)
    public static final String DATABASE_NAME = "complaint_app.db";
    public static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_COMPLAINTS = "complaints";
    public static final String TABLE_NOTICES = "notices";

    // Users table columns
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role";

    // Complaints table columns
    public static final String COL_COMPLAINT_ID = "id";
    public static final String COL_COMPLAINT_USER_ID = "user_id";
    public static final String COL_COMPLAINT_TITLE = "title";
    public static final String COL_COMPLAINT_CATEGORY = "category";
    public static final String COL_COMPLAINT_PRIORITY = "priority";
    public static final String COL_COMPLAINT_DESCRIPTION = "description";
    public static final String COL_COMPLAINT_STATUS = "status";
    public static final String COL_COMPLAINT_REPLY = "reply";
    public static final String COL_COMPLAINT_CREATED_DATE = "created_date";

    // Notices table columns
    public static final String COL_NOTICE_ID = "id";
    public static final String COL_NOTICE_TITLE = "title";
    public static final String COL_NOTICE_CONTENT = "content";
    public static final String COL_NOTICE_CREATED_DATE = "created_date";

    /**
     * Constructor that initializes SQLiteOpenHelper with the database name and version.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Enables foreign key constraints so ON DELETE CASCADE works when a user is deleted.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Called automatically the first time the database is created.
     * Creates the users, complaints, and notices tables, then inserts seed data.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_USER_PASSWORD + " TEXT NOT NULL, "
                + COL_USER_ROLE + " TEXT NOT NULL"
                + ");";

        String createComplaintsTable = "CREATE TABLE " + TABLE_COMPLAINTS + " ("
                + COL_COMPLAINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_COMPLAINT_USER_ID + " INTEGER NOT NULL, "
                + COL_COMPLAINT_TITLE + " TEXT NOT NULL, "
                + COL_COMPLAINT_CATEGORY + " TEXT NOT NULL, "
                + COL_COMPLAINT_PRIORITY + " TEXT NOT NULL, "
                + COL_COMPLAINT_DESCRIPTION + " TEXT NOT NULL, "
                + COL_COMPLAINT_STATUS + " TEXT NOT NULL, "
                + COL_COMPLAINT_REPLY + " TEXT, "
                + COL_COMPLAINT_CREATED_DATE + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_COMPLAINT_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"
                + ");";

        String createNoticesTable = "CREATE TABLE " + TABLE_NOTICES + " ("
                + COL_NOTICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOTICE_TITLE + " TEXT NOT NULL, "
                + COL_NOTICE_CONTENT + " TEXT NOT NULL, "
                + COL_NOTICE_CREATED_DATE + " TEXT NOT NULL"
                + ");";

        db.execSQL(createUsersTable);
        db.execSQL(createComplaintsTable);
        db.execSQL(createNoticesTable);

        seedInitialData(db);
    }

    /**
     * Called when DATABASE_VERSION is increased. Drops old tables and recreates them.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLAINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Inserts one default admin account and two sample campus notices on first install.
     */
    private void seedInitialData(SQLiteDatabase db) {
        // Seed default admin account: admin@uni.com / admin123
        ContentValues adminValues = new ContentValues();
        adminValues.put(COL_USER_NAME, "Admin");
        adminValues.put(COL_USER_EMAIL, "admin@uni.com");
        adminValues.put(COL_USER_PASSWORD, "admin123");
        adminValues.put(COL_USER_ROLE, Constants.ROLE_ADMIN);
        db.insert(TABLE_USERS, null, adminValues);

        String now = getCurrentDateTime();

        // Seed Notice 1
        ContentValues notice1 = new ContentValues();
        notice1.put(COL_NOTICE_TITLE, "Mid-term exam schedule & seating matrices announced");
        notice1.put(COL_NOTICE_CONTENT,
                "Seating arrangements for Hall Alpha, Beta, and Tech-Lab Quad have been published. "
                        + "Please bring your university ID card 20 minutes before the paper starts.");
        notice1.put(COL_NOTICE_CREATED_DATE, now);
        db.insert(TABLE_NOTICES, null, notice1);

        // Seed Notice 2
        ContentValues notice2 = new ContentValues();
        notice2.put(COL_NOTICE_TITLE, "24/7 Silent Study Wing Activation");
        notice2.put(COL_NOTICE_CONTENT,
                "Central Library floors 2 & 3 will remain accessible through midnight for revision "
                        + "cohorts. Night shuttle vans will dock at South Gate every 30 minutes.");
        notice2.put(COL_NOTICE_CREATED_DATE, now);
        db.insert(TABLE_NOTICES, null, notice2);
    }

    /**
     * Helper method that returns the current date and time formatted as "yyyy-MM-dd HH:mm".
     */
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        return sdf.format(new Date());
    }

    // =========================================================================
    // USER METHODS
    // =========================================================================

    /**
     * Registers a new student user with role "user". Returns true if insert succeeded.
     */
    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name.trim());
        values.put(COL_USER_EMAIL, email.trim().toLowerCase(Locale.US));
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_ROLE, Constants.ROLE_USER);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Checks email and password for login. Returns the matching User object, or null if invalid.
     */
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanEmail = email.trim().toLowerCase(Locale.US);

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_USER_EMAIL + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{cleanEmail, password},
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    /**
     * Checks if an email address is already registered in the users table.
     */
    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cleanEmail = email.trim().toLowerCase(Locale.US);

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + " = ?",
                new String[]{cleanEmail},
                null,
                null,
                null
        );

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToFirst();
            cursor.close();
        }
        return exists;
    }

    /**
     * Returns a single User by their ID, or null if not found.
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    /**
     * Returns a list of all registered users ordered by ID.
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                COL_USER_ID + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                userList.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return userList;
    }

    /**
     * Deletes a user by ID (admin accounts are protected and cannot be deleted).
     * Because of ON DELETE CASCADE, the user's complaints are automatically deleted too.
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_USERS,
                COL_USER_ID + " = ? AND " + COL_USER_ROLE + " != ?",
                new String[]{String.valueOf(userId), Constants.ROLE_ADMIN}
        );
        return rows > 0;
    }

    /**
     * Updates the password for the given user ID. Returns true if updated.
     */
    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_PASSWORD, newPassword);

        int rows = db.update(
                TABLE_USERS,
                values,
                COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        return rows > 0;
    }

    /**
     * Helper method that converts the current Cursor row into a User object.
     */
    private User cursorToUser(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
        String role = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE));
        return new User(id, name, email, password, role);
    }

    // =========================================================================
    // COMPLAINT METHODS
    // =========================================================================

    /**
     * Inserts a new complaint with initial status "Pending" and current date/time.
     * Returns the new row ID, or -1 if insert failed.
     */
    public long addComplaint(int userId, String title, String category, String priority, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLAINT_USER_ID, userId);
        values.put(COL_COMPLAINT_TITLE, title.trim());
        values.put(COL_COMPLAINT_CATEGORY, category);
        values.put(COL_COMPLAINT_PRIORITY, priority);
        values.put(COL_COMPLAINT_DESCRIPTION, description.trim());
        values.put(COL_COMPLAINT_STATUS, Constants.STATUS_PENDING);
        values.putNull(COL_COMPLAINT_REPLY);
        values.put(COL_COMPLAINT_CREATED_DATE, getCurrentDateTime());

        return db.insert(TABLE_COMPLAINTS, null, values);
    }

    /**
     * Returns all complaints submitted by a specific student (newest first).
     */
    public List<Complaint> getComplaintsByUser(int userId) {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT c.*, u." + COL_USER_NAME + " AS student_name "
                + "FROM " + TABLE_COMPLAINTS + " c "
                + "LEFT JOIN " + TABLE_USERS + " u ON c." + COL_COMPLAINT_USER_ID + " = u." + COL_USER_ID + " "
                + "WHERE c." + COL_COMPLAINT_USER_ID + " = ? "
                + "ORDER BY c." + COL_COMPLAINT_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToComplaint(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Returns all complaints from all students (newest first), joined with student names.
     */
    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT c.*, u." + COL_USER_NAME + " AS student_name "
                + "FROM " + TABLE_COMPLAINTS + " c "
                + "LEFT JOIN " + TABLE_USERS + " u ON c." + COL_COMPLAINT_USER_ID + " = u." + COL_USER_ID + " "
                + "ORDER BY c." + COL_COMPLAINT_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToComplaint(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Returns complaints filtered by status ("Pending" or "Resolved"), newest first.
     */
    public List<Complaint> getComplaintsByStatus(String status) {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT c.*, u." + COL_USER_NAME + " AS student_name "
                + "FROM " + TABLE_COMPLAINTS + " c "
                + "LEFT JOIN " + TABLE_USERS + " u ON c." + COL_COMPLAINT_USER_ID + " = u." + COL_USER_ID + " "
                + "WHERE c." + COL_COMPLAINT_STATUS + " = ? "
                + "ORDER BY c." + COL_COMPLAINT_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, new String[]{status});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToComplaint(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Returns the most recent N complaints for the Admin Overview "Recent Filings" list.
     */
    public List<Complaint> getRecentComplaints(int limit) {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT c.*, u." + COL_USER_NAME + " AS student_name "
                + "FROM " + TABLE_COMPLAINTS + " c "
                + "LEFT JOIN " + TABLE_USERS + " u ON c." + COL_COMPLAINT_USER_ID + " = u." + COL_USER_ID + " "
                + "ORDER BY c." + COL_COMPLAINT_ID + " DESC LIMIT ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(limit)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToComplaint(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Returns a single complaint by its ID (including the student's name), or null if not found.
     */
    public Complaint getComplaintById(int complaintId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT c.*, u." + COL_USER_NAME + " AS student_name "
                + "FROM " + TABLE_COMPLAINTS + " c "
                + "LEFT JOIN " + TABLE_USERS + " u ON c." + COL_COMPLAINT_USER_ID + " = u." + COL_USER_ID + " "
                + "WHERE c." + COL_COMPLAINT_ID + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(complaintId)});
        Complaint complaint = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                complaint = cursorToComplaint(cursor);
            }
            cursor.close();
        }
        return complaint;
    }

    /**
     * Updates a complaint's title, description, and priority (used by EditComplaintActivity).
     */
    public boolean updateComplaint(int complaintId, String title, String description, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLAINT_TITLE, title.trim());
        values.put(COL_COMPLAINT_DESCRIPTION, description.trim());
        values.put(COL_COMPLAINT_PRIORITY, priority);

        int rows = db.update(
                TABLE_COMPLAINTS,
                values,
                COL_COMPLAINT_ID + " = ?",
                new String[]{String.valueOf(complaintId)}
        );
        return rows > 0;
    }

    /**
     * Updates a complaint's title, category, priority, and description.
     */
    public boolean updateComplaint(int complaintId, String title, String category, String priority, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLAINT_TITLE, title.trim());
        values.put(COL_COMPLAINT_CATEGORY, category);
        values.put(COL_COMPLAINT_PRIORITY, priority);
        values.put(COL_COMPLAINT_DESCRIPTION, description.trim());

        int rows = db.update(
                TABLE_COMPLAINTS,
                values,
                COL_COMPLAINT_ID + " = ?",
                new String[]{String.valueOf(complaintId)}
        );
        return rows > 0;
    }

    /**
     * Saves the admin's reply and updates the complaint status ("Pending" or "Resolved").
     */
    public boolean replyToComplaint(int complaintId, String reply, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMPLAINT_REPLY, reply != null ? reply.trim() : "");
        values.put(COL_COMPLAINT_STATUS, status);

        int rows = db.update(
                TABLE_COMPLAINTS,
                values,
                COL_COMPLAINT_ID + " = ?",
                new String[]{String.valueOf(complaintId)}
        );
        return rows > 0;
    }

    /**
     * Deletes a complaint by its ID. Returns true if a row was deleted.
     */
    public boolean deleteComplaint(int complaintId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_COMPLAINTS,
                COL_COMPLAINT_ID + " = ?",
                new String[]{String.valueOf(complaintId)}
        );
        return rows > 0;
    }

    /**
     * Helper method that converts the current Cursor row into a Complaint object.
     */
    private Complaint cursorToComplaint(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLAINT_ID));
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLAINT_USER_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_TITLE));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_CATEGORY));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_PRIORITY));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_DESCRIPTION));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_STATUS));
        String reply = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_REPLY));
        String createdDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPLAINT_CREATED_DATE));

        Complaint complaint = new Complaint(id, userId, title, category, priority, description, status, reply, createdDate);

        int nameIndex = cursor.getColumnIndex("student_name");
        if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
            complaint.setUserName(cursor.getString(nameIndex));
        }
        return complaint;
    }

    // =========================================================================
    // NOTICE METHODS
    // =========================================================================

    /**
     * Inserts a new official notice with the current date/time. Returns the new row ID.
     */
    public long addNotice(String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTICE_TITLE, title.trim());
        values.put(COL_NOTICE_CONTENT, content.trim());
        values.put(COL_NOTICE_CREATED_DATE, getCurrentDateTime());

        return db.insert(TABLE_NOTICES, null, values);
    }

    /**
     * Returns all notices ordered from newest to oldest.
     */
    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NOTICES,
                null,
                null,
                null,
                null,
                null,
                COL_NOTICE_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToNotice(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Returns a single notice by its ID, or null if not found.
     */
    public Notice getNoticeById(int noticeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NOTICES,
                null,
                COL_NOTICE_ID + " = ?",
                new String[]{String.valueOf(noticeId)},
                null,
                null,
                null
        );

        Notice notice = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                notice = cursorToNotice(cursor);
            }
            cursor.close();
        }
        return notice;
    }

    /**
     * Updates an existing notice's title and content. Returns true if updated.
     */
    public boolean updateNotice(int noticeId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTICE_TITLE, title.trim());
        values.put(COL_NOTICE_CONTENT, content.trim());

        int rows = db.update(
                TABLE_NOTICES,
                values,
                COL_NOTICE_ID + " = ?",
                new String[]{String.valueOf(noticeId)}
        );
        return rows > 0;
    }

    /**
     * Deletes a notice by its ID. Returns true if deleted.
     */
    public boolean deleteNotice(int noticeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_NOTICES,
                COL_NOTICE_ID + " = ?",
                new String[]{String.valueOf(noticeId)}
        );
        return rows > 0;
    }

    /**
     * Helper method that converts the current Cursor row into a Notice object.
     */
    private Notice cursorToNotice(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTICE_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTICE_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTICE_CONTENT));
        String createdDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTICE_CREATED_DATE));
        return new Notice(id, title, content, createdDate);
    }

    // =========================================================================
    // STATISTICS METHODS (SQL COUNT queries for Overview & Profile cards)
    // =========================================================================

    /**
     * Returns the total number of complaints in the database using SQL COUNT(*).
     */
    public int getComplaintCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COMPLAINTS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Returns the number of complaints matching a status ("Pending" or "Resolved").
     */
    public int getComplaintCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_COMPLAINTS + " WHERE " + COL_COMPLAINT_STATUS + " = ?",
                new String[]{status}
        );
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Returns the total number of complaints submitted by a single user.
     */
    public int getComplaintCountByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_COMPLAINTS + " WHERE " + COL_COMPLAINT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Returns the number of complaints for a specific user and status (for User stat cards).
     */
    public int getComplaintCountByUserAndStatus(int userId, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_COMPLAINTS
                        + " WHERE " + COL_COMPLAINT_USER_ID + " = ? AND " + COL_COMPLAINT_STATUS + " = ?",
                new String[]{String.valueOf(userId), status}
        );
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Returns the total number of official notices in the database.
     */
    public int getNoticeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTICES, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Returns the total number of registered users in the database.
     */
    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }
}
