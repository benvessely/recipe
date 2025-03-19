package benv.recipe.model;

import java.util.List;
import benv.recipe.model.NutritionValuesModel;

public class RecipeNutritionModel {
    private List<IngredientMatchModel> ingredients;
    private Integer servings;
    private NutritionValuesModel totalNutrients;
    private NutritionValuesModel perServingNutrients;

    public List<IngredientMatchModel> getIngredients() {
        return ingredients;
    }

    public Integer getServings() {
        return servings;
    }

    public NutritionValuesModel getTotalNutrients() {
        return totalNutrients;
    }

    public NutritionValuesModel getPerServingNutrients() {
        return perServingNutrients;
    }

    public void setIngredients(List<IngredientMatchModel> ingredients) {
        this.ingredients = ingredients;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setTotalNutrients(NutritionValuesModel totalNutrients) {
        this.totalNutrients = totalNutrients;
    }

    public void setPerServingNutrients(NutritionValuesModel perServingNutrients) {
        this.perServingNutrients = perServingNutrients;
    }
}