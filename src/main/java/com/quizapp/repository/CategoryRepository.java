package com.quizapp.repository;

import com.quizapp.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for Category persistence operations.
 */
public interface CategoryRepository {

    /** Persist a new category and return generated id. */
    int save(Category category);

    /** Find category by id. */
    Optional<Category> findById(int id);

    /** Find category by name. */
    Optional<Category> findByName(String name);

    /** Return all categories. */
    List<Category> findAll();

    /** Update a category. */
    boolean update(Category category);

    /** Delete a category by id. */
    boolean delete(int id);
}
