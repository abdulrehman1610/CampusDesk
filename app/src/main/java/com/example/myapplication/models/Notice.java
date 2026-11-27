package com.example.myapplication.models;

import java.util.Locale;

/**
 * Notice model class that represents one row from the "notices" table in SQLite.
 * Notices are posted by the admin and read by all students.
 */
public class Notice {

    private int id;
    private String title;
    private String content;
    private String createdDate;

    /**
     * Empty constructor for creating a Notice object and setting fields later.
     */
    public Notice() {
    }

    /**
     * Full constructor used when reading a notice row from the database.
     */
    public Notice(int id, String title, String content, String createdDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
    }

    /**
     * Returns the unique notice ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique notice ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the notice title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the notice title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the full text content of the notice.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the full text content of the notice.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the date and time when the notice was published.
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the date and time when the notice was published.
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Formats the numeric ID as an official notice code like "NTC-0001" for the card badge.
     */
    public String getNoticeCode() {
        return String.format(Locale.US, "NTC-%04d", id);
    }
}
