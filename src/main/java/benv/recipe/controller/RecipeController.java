package benv.recipe.controller;

import benv.recipe.model.RecipeModel;
import benv.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // Spring automatically converts JSON from request into RecipeModel object
    @PutMapping
    public ResponseEntity<RecipeModel> createRecipe(@RequestBody RecipeModel recipe) {
        logger.info("PUT request received, running createRecipe");
        RecipeModel created = recipeService.createRecipe(recipe);
        return ResponseEntity.ok(created);
    }

    // Spring automatically converts JSON from request into RecipeModel object
    @PutMapping("/{id}")
    public ResponseEntity<RecipeModel> updateRecipe(@PathVariable Integer id,
                                                    @RequestBody RecipeModel recipe) {
        logger.info("PUT update request received, running updateRecipe with id {}", id);
        RecipeModel updated = recipeService.updateRecipe(id, recipe);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<RecipeModel>> getAllRecipes() {
        logger.info("GET request received, running getAllRecipes()");
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeModel> getRecipe(@PathVariable Integer id) {
        logger.info("GET request received with id {}, running getRecipe", id);
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }
}