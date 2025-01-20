package benv.recipe.service;

import benv.recipe.model.RecipeModel;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RecipeService {
    private final List<RecipeModel> recipes = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong();
    
    public RecipeModel createRecipe(RecipeModel recipe) {
        recipe.setId(idCounter.incrementAndGet());
        recipes.add(recipe);
        return recipe;
    }
    
    public List<RecipeModel> getAllRecipes() {
        return new ArrayList<>(recipes);
    }
    
    public RecipeModel getRecipeById(Long id) {
        return recipes.stream()
            .filter(recipe -> recipe.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }
}
