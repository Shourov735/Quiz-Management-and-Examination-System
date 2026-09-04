package com.quizapp.model;

import java.util.Objects;

/**
 * Represents a subject-area category used to organise quizzes and questions
 * (e.g. "Programming", "Database", "Operating Systems").
 */
public class Category {

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------

    private int    id;
    private String name;
    private String description;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** No-argument constructor required by mapping frameworks. */
    public Category() {
    }

    /**
     * Convenience constructor for creating a category without a database id
     * (e.g. before persisting).
     *
     * @param name        unique category name
     * @param description optional human-readable description
     */
    public Category(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    /**
     * Full constructor.
     *
     * @param id          database primary key
     * @param name        unique category name
     * @param description optional human-readable description
     */
    public Category(int id, String name, String description) {
        this.id          = id;
        this.name        = name;
        this.description = description;
    }

    // -------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------

    /**
     * Returns the primary-key identifier.
     *
     * @return category id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the primary-key identifier.
     *
     * @param id category id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the unique category name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name unique name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the optional category description.
     *
     * @return description, or {@code null} if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the category description.
     *
     * @param description human-readable description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // -------------------------------------------------------
    // Identity & Presentation
    // -------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category other)) return false;
        return id == other.id && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Category{id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + '}';
    }
}
