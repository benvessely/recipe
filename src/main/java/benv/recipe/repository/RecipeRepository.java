package benv.recipe.repository;

import benv.recipe.model.RecipeModel;
import io.micrometer.common.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;

import java.util.List;
import java.util.Map;

@Repository
public class RecipeRepository {
    private final JdbcTemplate jdbcTemplate;

    private static class RecipeRowMapper implements RowMapper<RecipeModel> {
        @Override
        public RecipeModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            RecipeModel recipe = new RecipeModel();
            recipe.setId(rs.getInt("id"));
            recipe.setTitle(rs.getString("title"));
            recipe.setDescription(rs.getString("description"));
            recipe.setIngredients(rs.getString("ingredients"));
            recipe.setInstructions(rs.getString("instructions"));
            recipe.setServings(rs.getInt("servings"));
            recipe.setPrepTimeMinutes(rs.getInt("prep_time_minutes"));
            recipe.setCookTimeMinutes(rs.getInt("cook_time_minutes"));
            recipe.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                recipe.setUpdatedAt(updatedAt.toLocalDateTime());
            }
            return recipe;
        }
    };

    private final RowMapper<RecipeModel> recipeRowMapper = new RecipeRowMapper();

    @Autowired
    public RecipeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RecipeModel createRecipe(RecipeModel recipe) {
        String sql = """
            INSERT INTO recipes (title, description, ingredients, instructions, 
                               servings, prep_time_minutes, cook_time_minutes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Lambda implements createPreparedStatement method of the
        // PreparedStatementCreator interface, which results in an object
        // with reference type PreparedStatementCreator but really anonymous
        // type being passed into update.
        jdbcTemplate.update( (Connection connection) -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, recipe.getTitle());
            ps.setString(2, recipe.getDescription());
            ps.setString(3, recipe.getIngredients());
            ps.setString(4, recipe.getInstructions());
            ps.setObject(5, recipe.getServings());
            ps.setObject(6, recipe.getPrepTimeMinutes());
            ps.setObject(7, recipe.getCookTimeMinutes());
            return ps;
        }, keyHolder);

        recipe.setId((Integer) keyHolder.getKey());
        return getRecipeById(recipe.getId());
    }

    public List<RecipeModel> getAllRecipes() {
        return jdbcTemplate.query(
                "SELECT * FROM recipes ORDER BY created_at DESC",
                recipeRowMapper
        );
    }

    public RecipeModel getRecipeById(Integer id) {
        List<RecipeModel> results = jdbcTemplate.query(
                "SELECT * FROM recipes WHERE id = ?",
                recipeRowMapper
                id
        );

        if (results.isEmpty()) {
            throw new RuntimeException("Recipe not found");
        }

        return results.get(0);
    }

    public RecipeModel updateRecipe(Integer id, RecipeModel recipe) {
        String sql = """
            UPDATE recipes
            SET title = ?, description = ?, ingredients = ?,
                instructions = ?, servings = ?, prep_time_minutes = ?,
                cook_time_minutes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        int rowsAffected = jdbcTemplate.update(sql,
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getIngredients(),
                recipe.getInstructions(),
                recipe.getServings(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                id
        );

        if (rowsAffected == 0) {
            throw new RuntimeException("Recipe not found");
        }

        return getRecipeById(id);
    }
}
