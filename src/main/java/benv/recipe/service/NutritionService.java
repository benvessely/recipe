package benv.recipe.service;

import benv.recipe.model.IngredientMatchModel;
import benv.recipe.model.IngredientSelectionModel;
import benv.recipe.model.NutritionValuesModel;
import benv.recipe.model.RecipeNutritionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class NutritionService {
    private final JdbcTemplate jdbcTemplate;
    private final PortionService portionService;
    private final WeightConverter weightConverter;
    private static final Logger logger = LoggerFactory.getLogger(IngredientMatchService.class);

    public NutritionService(JdbcTemplate jdbcTemplate,
                            PortionService portionService,
                            WeightConverter weightConverter) {
        this.jdbcTemplate = jdbcTemplate;
        this.portionService = portionService;
        this.weightConverter = weightConverter;
    }


    public RecipeNutritionModel calculateNutrition(List<IngredientSelectionModel> selections) {
        RecipeNutritionModel nutrition = new RecipeNutritionModel();

        NutritionValuesModel totalNutrients = new NutritionValuesModel();
        totalNutrients.setCalories(0.0);
        totalNutrients.setProtein(0.0);
        totalNutrients.setFat(0.0);
        totalNutrients.setCarbs(0.0);
        totalNutrients.setFiber(0.0);
        totalNutrients.setTotalSugar(0.0);
        totalNutrients.setSatFat(0.0);
        totalNutrients.setCholesterol(0.0);
        totalNutrients.setSodium(0.0);

        for (IngredientSelectionModel selection : selections) {
            Double weightGrams = weightConverter.convertToGrams(selection);

            // Query the database for nutrition data for this ingredient
            String sql = """
                SELECT 
                    name,
                    calories_per_100g, 
                    protein_per_100g, 
                    fat_per_100g, 
                    carbs_per_100g, 
                    fiber_per_100g, 
                    total_sugar_per_100g, 
                    sat_fat_per_100g, 
                    cholesterol_per_100g, 
                    sodium_per_100g 
                FROM ingredients 
                WHERE fdc_id = ?
                """;

            Map<String, Object> nutritionData = jdbcTemplate.queryForMap(sql, selection.getFdcId());

            Object calories_per_100g = nutritionData.get("calories_per_100g");
            if (calories_per_100g != null) {
                // Nutrient amount total Units = weight g * \left( \frac{nutrient amount Units}{100 g} \right)
                Double amountNew = weightGrams / 100.0  * ((Number) calories_per_100g).doubleValue();
                totalNutrients.setCalories(totalNutrients.getCalories() + amountNew);
                logger.info("Added calories, totalNutrients.getCalories is now {}", totalNutrients.getCalories());
            }
            Object protein_per_100g = nutritionData.get("protein_per_100g");
            if (protein_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) protein_per_100g).doubleValue();
                totalNutrients.setProtein(totalNutrients.getProtein() + amountNew);
            }
            Object fat_per_100g = nutritionData.get("fat_per_100g");
            if (fat_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) fat_per_100g).doubleValue();
                totalNutrients.setFat(totalNutrients.getFat() + amountNew);
            }
            Object carbs_per_100g = nutritionData.get("carbs_per_100g");
            if (carbs_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) carbs_per_100g).doubleValue();
                totalNutrients.setCarbs(totalNutrients.getCarbs() + amountNew);
            }
            Object fiber_per_100g = nutritionData.get("fiber_per_100g");
            if (fiber_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) fiber_per_100g).doubleValue();
                totalNutrients.setFiber(totalNutrients.getFiber() + amountNew);
            }
            Object total_sugar_per_100g = nutritionData.get("total_sugar_per_100g");
            if (total_sugar_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) total_sugar_per_100g).doubleValue();
                totalNutrients.setTotalSugar(totalNutrients.getTotalSugar() + amountNew);
            }
            Object sat_fat_per_100g = nutritionData.get("sat_fat_per_100g");
            if (sat_fat_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) sat_fat_per_100g).doubleValue();
                totalNutrients.setSatFat(totalNutrients.getSatFat() + amountNew);
            }
            Object cholesterol_per_100g = nutritionData.get("cholesterol_per_100g");
            if (cholesterol_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) cholesterol_per_100g).doubleValue();
                totalNutrients.setCholesterol(totalNutrients.getCholesterol() + amountNew);
            }
            Object sodium_per_100g = nutritionData.get("sodium_per_100g");
            if (sodium_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) sodium_per_100g).doubleValue();
                totalNutrients.setSodium(totalNutrients.getSodium() + amountNew);
            }

        }
        nutrition.setTotalNutrients(totalNutrients);
        return nutrition;
    }
}
