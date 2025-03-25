package benv.recipe;

import benv.recipe.model.IngredientSelectionModel;
import benv.recipe.model.NutritionValuesModel;
import benv.recipe.model.RecipeNutritionModel;
import benv.recipe.service.IngredientMatchService;
import benv.recipe.service.NutritionService;
import benv.recipe.service.WeightConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class NutritionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(NutritionServiceTest.class);

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private WeightConverter weightConverter;

    @InjectMocks
    private NutritionService nutritionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSingle() {
        IngredientSelectionModel frankfurter = new IngredientSelectionModel();
        frankfurter.setPortionId(81778);
        frankfurter.setFdcId(167696);
        frankfurter.setQuantity(1.0);
        frankfurter.setUnit("frankfurter");
        
        List<IngredientSelectionModel> input = new ArrayList<>(List.of(frankfurter));

        when(weightConverter.convertToGrams(any())).thenReturn(57.0);

        Map<String, Object> nutritionData = new HashMap<>();
        nutritionData.put("calories_per_100g", 140.0);
        nutritionData.put("protein_per_100g", 12.0);
        nutritionData.put("fat_per_100g", 9.5);
        nutritionData.put("carbs_per_100g", 1.6);
        nutritionData.put("fiber_per_100g", 0.0);
        nutritionData.put("total_sugar_per_100g", 0.85);
        nutritionData.put("sat_fat_per_100g", 1.526);
        nutritionData.put("cholesterol_per_100g", 40.0);
        nutritionData.put("sodium_per_100g", 744.0);

        when(jdbcTemplate.queryForMap(anyString(), anyInt())).thenReturn(nutritionData);

        RecipeNutritionModel bigNutritionObject = nutritionService.calculateNutrition(input, 4);
        NutritionValuesModel totalNutrition = bigNutritionObject.getTotalNutrition();
        logger.info("totalNutrition is {}", totalNutrition);

        assertEquals(79.8, totalNutrition.getCalories(), 0.01, "One frankfurter should have 79.8 Calories");
        assertEquals(6.84, totalNutrition.getProtein(), 0.01, "One frankfurter should have 6.84g protein");
        assertEquals(5.415, totalNutrition.getFat(), 0.01, "One frankfurter should have 5.415g fat");
        assertEquals(0.912, totalNutrition.getCarbs(), 0.01, "One frankfurter should have 0.912g carbs");
        assertEquals(0, totalNutrition.getFiber(), 0.01, "One frankfurter should have 0 fiber");
        assertEquals(0.485, totalNutrition.getTotalSugar(), 0.01, "One frankfurter should have 0.485g total sugar");
        assertEquals(0.870, totalNutrition.getSatFat(), 0.01, "One frankfurter should have 0.87g sat fat");
        assertEquals(22.8, totalNutrition.getCholesterol(), 0.01, "One frankfurter should have 22.8 units cholesterol");
        assertEquals(424.08, totalNutrition.getSodium(), .01, "One frankfurter should have 424.08mg sodium");

    }


}
