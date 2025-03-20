package benv.recipe;

import benv.recipe.model.IngredientSelectionModel;
import benv.recipe.service.WeightConverter;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class WeightConverterTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WeightConverter weightConverter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConvertGrams() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(100.0);
        selection.setUnit("g");

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(100.0, grams, 0.01, "100g should convert to 100g");
    }

    @Test
    public void testConvertOunces() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(2.0);
        selection.setUnit("oz");

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(56.7, grams, 0.01, "2oz should convert to 56.7g");
    }

    @Test
    public void testConvertPounds() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(1.0);
        selection.setUnit("lb");

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(453.59, grams, 0.01, "1lb should convert to 453.59g");
    }

    @Test
    public void testConvertKilograms() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(0.5);
        selection.setUnit("kg");

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(500.0, grams, 0.01, "0.5kg should convert to 500g");
    }

    @Test
    public void testConvertCup() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(2.0);
        selection.setUnit("cup");
        selection.setPortionId(83110);

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Double.class),
                eq(83110)
        )).thenReturn(160.0);

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(320.0, grams, 0.01, "2 cups should convert to 320g");
    }

    @Test
    public void testConvertThing() {
        IngredientSelectionModel selection = new IngredientSelectionModel();
        selection.setQuantity(0.5);
        selection.setUnit("waffle, square");
        selection.setPortionId(81553);

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Double.class),
                eq(81553)
        )).thenReturn(39.0);

        Double grams = weightConverter.convertToGrams(selection);

        assertEquals(19.5, grams, 0.01, "Half of waffle should be 19.5g");
    }
}
