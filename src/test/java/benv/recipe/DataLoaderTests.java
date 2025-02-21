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

        // Verify some data was loaded
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ingredients",
                Integer.class
        );
        logger.info("Number of rows in ingredients table is {}", count);
        assertTrue(count != null && count==7793,
                String.format("ingredients table should have 7793 rows, instead has %d", count));
    }
}
