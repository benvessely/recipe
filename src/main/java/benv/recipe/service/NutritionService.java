package benv.recipe.service;

import benv.recipe.model.IngredientMatchModel;
import benv.recipe.model.IngredientSelectionModel;
import benv.recipe.model.RecipeNutritionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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

        for (IngredientSelectionModel selection : selections) {
            Double weightGrams = weightConverter.convertToGrams(selection);

        }
        return nutrition;
    }
}
