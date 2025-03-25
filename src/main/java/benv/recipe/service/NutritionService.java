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


    public RecipeNutritionModel calculateNutrition(List<IngredientSelectionModel> selections,
                                                   Integer servings) {
        RecipeNutritionModel nutrition = new RecipeNutritionModel();

        NutritionValuesModel totalNutrition = new NutritionValuesModel();
        totalNutrition.setCalories(0.0);
        totalNutrition.setProtein(0.0);
        totalNutrition.setFat(0.0);
        totalNutrition.setCarbs(0.0);
        totalNutrition.setFiber(0.0);
        totalNutrition.setTotalSugar(0.0);
        totalNutrition.setSatFat(0.0);
        totalNutrition.setCholesterol(0.0);
        totalNutrition.setSodium(0.0);

        for (IngredientSelectionModel selection : selections) {
            Double weightGrams = weightConverter.convertToGrams(selection);
            logger.info("weightGrams is {}", weightGrams);

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
                // Nutrient amount total in Units = weight g * \left( \frac{nutrient amount in Units}{100 g} \right)
                Double amountNew = weightGrams / 100.0  * ((Number) calories_per_100g).doubleValue();
                totalNutrition.setCalories(totalNutrition.getCalories() + amountNew);
                logger.info("Added calories, totalNutrition.getCalories is now {}", totalNutrition.getCalories());
            }
            Object protein_per_100g = nutritionData.get("protein_per_100g");
            if (protein_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) protein_per_100g).doubleValue();
                totalNutrition.setProtein(totalNutrition.getProtein() + amountNew);
            }
            Object fat_per_100g = nutritionData.get("fat_per_100g");
            if (fat_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) fat_per_100g).doubleValue();
                totalNutrition.setFat(totalNutrition.getFat() + amountNew);
            }
            Object carbs_per_100g = nutritionData.get("carbs_per_100g");
            if (carbs_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) carbs_per_100g).doubleValue();
                totalNutrition.setCarbs(totalNutrition.getCarbs() + amountNew);
            }
            Object fiber_per_100g = nutritionData.get("fiber_per_100g");
            if (fiber_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) fiber_per_100g).doubleValue();
                totalNutrition.setFiber(totalNutrition.getFiber() + amountNew);
            }
            Object total_sugar_per_100g = nutritionData.get("total_sugar_per_100g");
            if (total_sugar_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) total_sugar_per_100g).doubleValue();
                totalNutrition.setTotalSugar(totalNutrition.getTotalSugar() + amountNew);
            }
            Object sat_fat_per_100g = nutritionData.get("sat_fat_per_100g");
            if (sat_fat_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) sat_fat_per_100g).doubleValue();
                totalNutrition.setSatFat(totalNutrition.getSatFat() + amountNew);
            }
            Object cholesterol_per_100g = nutritionData.get("cholesterol_per_100g");
            if (cholesterol_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) cholesterol_per_100g).doubleValue();
                totalNutrition.setCholesterol(totalNutrition.getCholesterol() + amountNew);
            }
            Object sodium_per_100g = nutritionData.get("sodium_per_100g");
            if (sodium_per_100g != null) {
                Double amountNew = weightGrams / 100.0  * ((Number) sodium_per_100g).doubleValue();
                totalNutrition.setSodium(totalNutrition.getSodium() + amountNew);
            }

        }
        nutrition.setTotalNutrition(totalNutrition);

        if (servings != null) {
            nutrition.setServings(servings);

            NutritionValuesModel perServingNutrition = new NutritionValuesModel();
            perServingNutrition.setCalories(totalNutrition.getCalories() / servings);
            perServingNutrition.setProtein(totalNutrition.getProtein() / servings);
            perServingNutrition.setFat(totalNutrition.getFat() / servings);
            perServingNutrition.setCarbs(totalNutrition.getCarbs() / servings);
            perServingNutrition.setFiber(totalNutrition.getFiber() / servings);
            perServingNutrition.setTotalSugar(totalNutrition.getTotalSugar() / servings);
            perServingNutrition.setSatFat(totalNutrition.getSatFat() / servings);
            perServingNutrition.setCholesterol(totalNutrition.getCholesterol() / servings);
            perServingNutrition.setSodium(totalNutrition.getSodium() / servings);

            nutrition.setPerServingNutrition(perServingNutrition);
        }

        return nutrition;
    }
}
