package com.example.myapplication.utils;

/**
 * Constants holds every fixed value used in more than one place:
 * role names, complaint statuses, priorities, categories, Intent extra keys
 * and SharedPreferences keys. Keeping them here avoids typing mistakes
 * like "Pending" in one file and "pending" in another.
 */
public class Constants {

    // ----- User roles (stored in the "role" column of the users table) -----
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    // ----- Complaint statuses -----
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_RESOLVED = "Resolved";

    // ----- Complaint priorities -----
    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_HIGH = "High";

    // ----- Complaint categories (names from the Figma design) -----
    public static final String CATEGORY_HOSTEL = "Hostel & Mess";
    public static final String CATEGORY_TRANSPORT = "Transport";
    public static final String CATEGORY_ACADEMIC = "Academic";
    public static final String CATEGORY_FEES = "Fee & Dues";
    public static final String CATEGORY_LIBRARY = "Library";
    public static final String CATEGORY_GENERAL = "General";

    // All categories in the order they appear in the GridView
    public static final String[] CATEGORIES = {
            CATEGORY_HOSTEL,
            CATEGORY_TRANSPORT,
            CATEGORY_ACADEMIC,
            CATEGORY_FEES,
            CATEGORY_LIBRARY,
            CATEGORY_GENERAL
    };

    // ----- Intent extra keys (used with putExtra / getIntExtra) -----
    public static final String EXTRA_NOTICE_ID = "extra_notice_id";
    public static final String EXTRA_COMPLAINT_ID = "extra_complaint_id";
    public static final String EXTRA_MODE = "extra_mode";

    // Values for EXTRA_MODE on AddEditNoticeActivity
    public static final String MODE_ADD = "add";
    public static final String MODE_EDIT = "edit";

    // ----- SharedPreferences -----
    public static final String PREFS_NAME = "campus_desk_prefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_ALERTS_ENABLED = "alerts_enabled";

    // ----- Validation -----
    public static final int MIN_PASSWORD_LENGTH = 6;

    // ----- Date format saved in the database -----
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    // Campus help desk phone number used by the "Call Desk" button (ACTION_DIAL)
    public static final String HELP_DESK_PHONE = "tel:+924235761999";

    /**
     * Private constructor so nobody creates a Constants object.
     * This class is only used for its static values.
     */
    private Constants() {
    }
}
