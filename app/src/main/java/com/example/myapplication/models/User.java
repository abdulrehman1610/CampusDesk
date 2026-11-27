package com.example.myapplication.models;

/**
 * User model class that represents one row from the "users" table in SQLite.
 * A user can either be a normal student ("user") or an administrator ("admin").
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;
    private String role;

    /**
     * Empty constructor needed when creating a User object and setting fields later.
     */
    public User() {
    }

    /**
     * Full constructor used when reading a user row from the database.
     */
    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * Returns the unique user ID from the database.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique user ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the full name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the user's university email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's university email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's role ("user" or "admin").
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role ("user" or "admin").
     */
    public void setRole(String role) {
        this.role = role;
    }
}
