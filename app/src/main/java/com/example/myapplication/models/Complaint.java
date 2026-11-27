package com.example.myapplication.models;

import java.util.Locale;

/**
 * Complaint model class that represents one row from the "complaints" table in SQLite.
 * It also stores an optional userName field when joined with the "users" table for the admin view.
 */
public class Complaint {

    private int id;
    private int userId;
    private String userName;
    private String title;
    private String category;
    private String priority;
    private String description;
    private String status;
    private String reply;
    private String createdDate;

    /**
     * Empty constructor for creating a Complaint object and filling fields with setters.
     */
    public Complaint() {
    }

    /**
     * Full constructor used when reading a complaint row from the database.
     */
    public Complaint(int id, int userId, String title, String category, String priority,
                     String description, String status, String reply, String createdDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.description = description;
        this.status = status;
        this.reply = reply;
        this.createdDate = createdDate;
    }

    /**
     * Returns the unique complaint ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique complaint ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the ID of the student who submitted this complaint.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the student who submitted this complaint.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the student's name (populated in admin queries that join with the users table).
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the student's name for display in the admin complaints list.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the complaint title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the complaint title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the complaint category (e.g. Hostel & Mess, Transport, Academic).
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the complaint category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the urgency priority ("Low", "Medium", or "High").
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the urgency priority.
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Returns the full complaint description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the full complaint description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the current complaint status ("Pending" or "Resolved").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current complaint status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the admin reply text, or null/empty if the admin has not replied yet.
     */
    public String getReply() {
        return reply;
    }

    /**
     * Sets the admin reply text.
     */
    public void setReply(String reply) {
        this.reply = reply;
    }

    /**
     * Returns the date and time when the complaint was created.
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the date and time when the complaint was created.
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Formats the numeric ID as a ticket badge code like "#C-0001" to match the Figma design.
     */
    public String getTicketCode() {
        return String.format(Locale.US, "#C-%04d", id);
    }
}
