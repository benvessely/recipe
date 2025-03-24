package benv.recipe.controller;

import benv.recipe.model.*;
import benv.recipe.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class NutritionController {
    private final IngredientParserService ingredientParserService;
    private final RecipeService recipeService;
    private final IngredientMatchService ingredientMatchService;
    private final PortionService portionService;
    private final NutritionService nutritionService;

    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);

    @Autowired
    public NutritionController(IngredientParserService ingredientParserService,
                                RecipeService recipeService,
                                IngredientMatchService ingredientMatchService,
                                PortionService portionService,
                                NutritionService nutritionService) {
        this.ingredientParserService = ingredientParserService;
        this.recipeService = recipeService;
        this.ingredientMatchService = ingredientMatchService;
        this.portionService = portionService;
        this.nutritionService = nutritionService;
    }

    @GetMapping("/{id}/ingredient-matches")
    public ResponseEntity<Map<String, List<IngredientMatchModel>>> getMatches(@PathVariable int id) {
        RecipeModel recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(ingredientMatchService.fetchMatches(recipe));
    }

    @GetMapping("/portions")
    public ResponseEntity<Map<String,List<PortionModel>>> getPortionsForIngredients(
            @RequestParam List<Integer> fdcIds) {
        Map<String, List<PortionModel>> portions = portionService.getPortions(fdcIds);
        return ResponseEntity.ok(portions);
    }

    // Returns ResponseEntity<RecipeNutritionModel> if inputs are valid, otherwise
    // returns ResponseEntity<String>
    @PutMapping("/calculate-nutrition")
    public ResponseEntity<?> calculateNutrition(
            @RequestBody List<IngredientSelectionModel> selections) {

        for (IngredientSelectionModel selection : selections) {
            if (selection.getQuantity() < 0) {
                logger.info("Returning 400 error, quantity for ingredients cannot be negative");
                return ResponseEntity.badRequest().body("Quantity for ingredients cannot be negative.");
            }
        }
        RecipeNutritionModel nutrition = nutritionService.calculateNutrition(selections);

        return ResponseEntity.ok(nutrition);
    }
}
