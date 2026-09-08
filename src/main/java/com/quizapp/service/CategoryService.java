package com.quizapp.service;

import com.quizapp.model.Category;
import com.quizapp.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Category} entities.
 *
 * <p>Provides CRUD operations delegating persistence to a
 * {@link CategoryRepository}.  Validation failures throw
 * {@link IllegalArgumentException}.</p>
 */
public class CategoryService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Underlying persistence layer for Category entities. */
    private final CategoryRepository categoryRepo;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code CategoryService} with the given category repository.
     *
     * @param categoryRepo the repository used for category persistence;
     *                     must not be {@code null}
     */
    public CategoryService(CategoryRepository categoryRepo) {
        if (categoryRepo == null) {
            throw new IllegalArgumentException("CategoryRepository must not be null.");
        }
        this.categoryRepo = categoryRepo;
    }

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    /**
     * Creates and persists a new {@link Category}.
     *
     * @param name        the unique category name; must be non-blank
     * @param description optional description; may be {@code null} or blank
     * @return the persisted {@link Category} with its generated id set
     * @throws IllegalArgumentException if {@code name} is blank or already exists
     */
    public Category createCategory(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank.");
        }

        String trimmedName = name.trim();
        if (categoryRepo.findByName(trimmedName).isPresent()) {
            throw new IllegalArgumentException(
                    "A category named '" + trimmedName + "' already exists.");
        }

        Category category = new Category();
        category.setName(trimmedName);
        category.setDescription(description != null ? description.trim() : "");

        int generatedId = categoryRepo.save(category);
        category.setId(generatedId);
        return category;
    }

    /**
     * Finds a {@link Category} by its primary key.
     *
     * @param id the category identifier
     * @return an {@link Optional} containing the category, or empty if not found
     */
    public Optional<Category> findById(int id) {
        return categoryRepo.findById(id);
    }

    /**
     * Returns all persisted categories.
     *
     * @return list of all {@link Category} records; never {@code null}
     */
    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    /**
     * Updates an existing {@link Category}.
     *
     * @param category the category to update; must have a valid id and non-blank name
     * @return the updated {@link Category}
     * @throws IllegalArgumentException if the category is {@code null} or its name is blank
     * @throws IllegalStateException    if no matching category record exists
     */
    public Category update(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null.");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank.");
        }

        if (categoryRepo.findById(category.getId()).isEmpty()) {
            throw new IllegalStateException(
                    "Category with id " + category.getId() + " does not exist.");
        }

        category.setName(category.getName().trim());
        boolean updated = categoryRepo.update(category);
        if (!updated) {
            throw new IllegalStateException(
                    "Failed to update category with id " + category.getId() + ".");
        }
        return category;
    }

    /**
     * Deletes a {@link Category} by its primary key.
     *
     * @param id the category identifier
     * @return {@code true} if the record was deleted, {@code false} if not found
     */
    public boolean delete(int id) {
        return categoryRepo.delete(id);
    }
}
