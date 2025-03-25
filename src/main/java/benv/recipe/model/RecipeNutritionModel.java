package benv.recipe.model;

import java.util.List;
import benv.recipe.model.NutritionValuesModel;

public class RecipeNutritionModel {
    private List<IngredientSelectionModel> selections;
    private Integer servings;
    private NutritionValuesModel totalNutrition;
    private NutritionValuesModel perServingNutrition;

    public List<IngredientSelectionModel> getSelections() {
        return selections;
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

    public void setSelections(List<IngredientSelectionModel> selections) {
        this.selections = selections;
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