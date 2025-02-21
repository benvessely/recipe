package benv.recipe;

import benv.recipe.controller.RecipeController;
import benv.recipe.service.DataLoaderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class DataLoaderTests {
    private static final Logger logger = LoggerFactory.getLogger(DataLoaderTests.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataLoaderService dataLoaderService;

    @Test
    void testDataLoading() {
        Boolean tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'ingredients')",
                Boolean.class
        );
        assertTrue(tableExists != null && tableExists, "Ingredients table should exist");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ingredients",
                Integer.class
        );
        logger.info("Number of rows in ingredients table is {}", count);
        assertTrue(count != null && count==7793,
                String.format("ingredients table should have 7793 rows, instead has %d", count));

        Integer countProtein = jdbcTemplate.queryForObject(
                "SELECT COUNT(protein_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countProtein != null && countProtein > 0,
                "Protein data didn't load");


        Integer countCalories = jdbcTemplate.queryForObject(
                "SELECT COUNT(calories_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countCalories != null && countCalories>0,
                "Calories data didn't load");


        Integer countFat = jdbcTemplate.queryForObject(
                "SELECT COUNT(fat_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countFat != null && countFat>0,
                "Fat data didn't load");


        Integer countCarbs = jdbcTemplate.queryForObject(
                "SELECT COUNT(carbs_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countCarbs!= null && countCarbs>0,
                "Carb data didn't load");


        Integer countFiber = jdbcTemplate.queryForObject(
                "SELECT COUNT(fiber_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countFiber!= null && countFiber>0,
                "Fiber data didn't load");

        Integer countSatFat = jdbcTemplate.queryForObject(
                "SELECT COUNT(sat_fat_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countSatFat!= null && countSatFat>0,
                "Saturated fat data didn't load");

        Integer countCholesterol = jdbcTemplate.queryForObject(
                "SELECT COUNT(cholesterol_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countCholesterol!= null && countCholesterol>0,
                "Cholesterol data didn't load");

        Integer countTotalSugar = jdbcTemplate.queryForObject(
                "SELECT COUNT(total_sugar_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countTotalSugar!= null && countTotalSugar>0,
                "Total sugar data didn't load");

        Integer countSodium = jdbcTemplate.queryForObject(
                "SELECT COUNT(sodium_per_100g) FROM ingredients",
                Integer.class
        );
        assertTrue(countSodium != null && countSodium>0,
                "Sodium data didn't load");


        Integer countPortionRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM portions",
                Integer.class
        );
        logger.info("Numer of rows in portion data is {}", countPortionRows);
        assertTrue(countPortionRows != null && countPortionRows == 14449,
                "Not all rows loaded for the portion data");
    }
}
