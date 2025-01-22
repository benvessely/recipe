package benv.recipe.service;

import benv.recipe.model.RecipeModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RecipeService {
    private final List<RecipeModel> recipes = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong();
    
    public RecipeModel createRecipe(RecipeModel recipe) {
        recipe.setId(idCounter.incrementAndGet());
        recipe.setCreatedAt(LocalDateTime.now());
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

    public RecipeModel updateRecipe (Long id, RecipeModel updatedRecipe) {
        RecipeModel existingRecipe = getRecipeById(id);
        int recipeIndex = recipes.indexOf(existingRecipe);

        updatedRecipe.setId(id);
        updatedRecipe.setCreatedAt(existingRecipe.getCreatedAt());
        updatedRecipe.setUpdatedAt(LocalDateTime.now());

        recipes.set(recipeIndex, updatedRecipe);
        return updatedRecipe;
    }
}
