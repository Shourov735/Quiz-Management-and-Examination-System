package com.quizapp.repository;

import com.quizapp.database.DatabaseConnection;
import com.quizapp.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link CategoryRepository}.
 */
public class SQLiteCategoryRepository implements CategoryRepository {

    private static final String INSERT_CATEGORY =
            "INSERT INTO categories (name, description) VALUES (?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT id, name, description FROM categories WHERE id = ?";

    private static final String SELECT_BY_NAME =
            "SELECT id, name, description FROM categories WHERE name = ?";

    private static final String SELECT_ALL =
            "SELECT id, name, description FROM categories ORDER BY name ASC";

    private static final String UPDATE_CATEGORY =
            "UPDATE categories SET name = ?, description = ? WHERE id = ?";

    private static final String DELETE_CATEGORY =
            "DELETE FROM categories WHERE id = ?";

    @Override
    public int save(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category to save cannot be null");
        }
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_CATEGORY, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getDescription());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("Saving category failed, no rows affected.");
                }
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        category.setId(id);
                        return id;
                    } else {
                        throw new RuntimeException("Saving category failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error persisting category: " + category.getName(), e);
        }
    }

    @Override
    public Optional<Category> findById(int id) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding category with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NAME)) {
                stmt.setString(1, name.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding category with name " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all categories", e);
        }
        return categories;
    }

    @Override
    public boolean update(Category category) {
        if (category == null || category.getId() <= 0) {
            throw new IllegalArgumentException("Cannot update null or unpersisted category");
        }
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_CATEGORY)) {
                stmt.setString(1, category.getName());
                stmt.setString(2, category.getDescription());
                stmt.setInt(3, category.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category with id " + category.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_CATEGORY)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category with id " + id, e);
        }
    }

    private Category mapResultSet(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}
