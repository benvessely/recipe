package benv.recipe.controller;

import benv.recipe.model.IngredientMatchModel;
import benv.recipe.model.IngredientModel;
import benv.recipe.model.PortionModel;
import benv.recipe.model.RecipeModel;
import benv.recipe.service.IngredientParserService;
import benv.recipe.service.IngredientMatchService;
import benv.recipe.service.PortionService;
import benv.recipe.service.RecipeService;
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

    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);

    @Autowired
    public NutritionController(IngredientParserService ingredientParserService,
                                RecipeService recipeService,
                                IngredientMatchService ingredientMatchService,
                                PortionService portionService) {
        this.ingredientParserService = ingredientParserService;
        this.recipeService = recipeService;
        this.ingredientMatchService = ingredientMatchService;
        this.portionService = portionService;
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
}
