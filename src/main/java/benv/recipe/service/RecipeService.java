package benv.recipe.service;

import benv.recipe.model.RecipeModel;
import org.springframework.stereotype.Service;

import benv.recipe.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public RecipeModel createRecipe(RecipeModel recipe) {
        return recipeRepository.createRecipe(recipe);
    }

    public List<RecipeModel> getAllRecipes() {
        return recipeRepository.getAllRecipes();
    }

    public RecipeModel getRecipeById(Integer id) {
        return recipeRepository.getRecipeById(id);
    }

    public RecipeModel updateRecipe(Integer id, RecipeModel recipe) {
        return recipeRepository.updateRecipe(id, recipe);
    }
}
