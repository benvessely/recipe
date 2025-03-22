package benv.recipe.model;

import java.util.List;
import benv.recipe.model.NutritionValuesModel;

public class RecipeNutritionModel {
    private List<IngredientMatchModel> ingredients;
    private Integer servings;
    private NutritionValuesModel totalNutrition;
    private NutritionValuesModel perServingNutrition;

    public List<IngredientMatchModel> getIngredients() {
        return ingredients;
    }

    public Integer getServings() {
        return servings;
    }

    public NutritionValuesModel getTotalNutrition() {
        return totalNutrition;
    }

    public NutritionValuesModel getPerServingNutrition() {
        return perServingNutrition;
    }

    public void setIngredients(List<IngredientMatchModel> ingredients) {
        this.ingredients = ingredients;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setTotalNutrition(NutritionValuesModel totalNutrition) {
        this.totalNutrition = totalNutrition;
    }

    public void setPerServingNutrition(NutritionValuesModel perServingNutrition) {
        this.perServingNutrition = perServingNutrition;
    }
}