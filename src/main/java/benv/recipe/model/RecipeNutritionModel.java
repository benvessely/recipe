package benv.recipe.model;

import java.util.List;
import benv.recipe.model.NutritionValuesModel;

public class RecipeNutritionModel {
    private List<IngredientMatchModel> ingredients;
    private Integer servings;
    private NutritionValuesModel totalNutrients;
    private NutritionValuesModel perServingNutrients;

}