package benv.recipe.controller;

import benv.recipe.model.RecipeModel;
import benv.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // Spring automatically converts JSON from request into RecipeModel object
    @PutMapping
    public ResponseEntity<RecipeModel> createRecipe(@RequestBody RecipeModel recipe) {
        RecipeModel created = recipeService.createRecipe(recipe);
        return ResponseEntity.ok(created);
    }

    // Spring automatically converts JSON from request into RecipeModel object
    @PutMapping("/{id}")
    public ResponseEntity<RecipeModel> updateRecipe(@PathVariable Integer id,
                                                    @RequestBody RecipeModel recipe) {
        RecipeModel updated = recipeService.updateRecipe(id, recipe);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<RecipeModel>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeModel> getRecipe(@PathVariable Integer id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }
}