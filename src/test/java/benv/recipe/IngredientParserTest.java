package benv.recipe;

import benv.recipe.model.IngredientModel;
import benv.recipe.service.IngredientParserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class IngredientParserTest {
    private static final Logger logger = LoggerFactory.getLogger(IngredientParserTest.class);

    @Autowired
    IngredientParserService ingredientParserService;

    @Test
    void test1() {
        String ingredientLine = "2 cups flour";
        IngredientModel result = ingredientParserService.parse(ingredientLine);
        logger.info(result.toString());
        assertEquals(2, result.getAmount());
        assertEquals("cups", result.getUnit());
        assertEquals("flour", result.getMainIngredient());
    }

    @Test
    void test2() {
        String ingredientLine = "1/2 tbsp Olive oil";
        IngredientModel result = ingredientParserService.parse(ingredientLine);
        logger.info(result.toString());
        assertEquals(0.5, result.getAmount());
        assertEquals("tbsp", result.getUnit());
        assertEquals("olive oil", result.getMainIngredient());
    }

    @Test
    void test3() {
        String ingredientLine = "Pinch salt";
        IngredientModel result = ingredientParserService.parse(ingredientLine);
        logger.info(result.toString());
        assertNull(result.getAmount());
        assertEquals("pinch", result.getUnit());
        assertEquals("salt", result.getMainIngredient());
    }
}
