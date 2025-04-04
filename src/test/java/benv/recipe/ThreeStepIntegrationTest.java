package benv.recipe;

import benv.recipe.model.*;
import benv.recipe.repository.RecipeRepository;
import benv.recipe.service.IngredientMatchService;
import benv.recipe.service.IngredientParserService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThreeStepIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(IngredientMatchService.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientParserService ingredientParserService;

    private RecipeModel testRecipe;
    private Integer recipeId;

    @BeforeEach
    void setUp() {
        testRecipe = new RecipeModel();
        testRecipe.setTitle("Test Recipe");
        testRecipe.setIngredients("0.5lb Black beans\n1 cup Worcesterhsire sauce\n1 g garlic");
        testRecipe.setInstructions("Cook food");
        RecipeModel createdRecipe = recipeRepository.createRecipe(testRecipe);
        recipeId = createdRecipe.getId();
    }

    @AfterEach
    void tearDown() {
        try {
            logger.info("Removing test recipe from database");
            recipeRepository.deleteRecipe(recipeId);
        } catch (Exception e) {
            logger.warn("Error cleaning up test data: {}", e.getMessage());
        }
    }

    @Test
    void testThreeStepWorkflow() {
        // Step 1: Test fetching of matches
        ResponseEntity<Map<String, List<IngredientMatchModel>>> matchesResponse =
                restTemplate.exchange(
                        "/api/recipes/{id}/ingredient-matches",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, List<IngredientMatchModel>>>() {},
                        recipeId
                );

        assertThat(matchesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, List<IngredientMatchModel>> matches = matchesResponse.getBody();
        assertThat(matches).isNotNull();

        // Step 2: Test fetching of portions for chosen matches

        Map<String,Integer> ingredientToIdMap = makeIngredientToIdMap(matches);

        List<Integer> selectedFdcIds = new ArrayList<>(ingredientToIdMap.values());
        assertThat(selectedFdcIds).isNotEmpty();

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/recipes/portions");
        // Add (possibly) multiple query parameters with name `fdcIds`, one for the
        // fdcId of each ingredient we've chosen to get portions of. These are converted
        // by the controller to a Java List
        for (Integer fdcId : selectedFdcIds) {
            builder.queryParam("fdcIds", fdcId);
        }
        URI uri = builder.build().encode().toUri();

        ResponseEntity<Map<Integer, List<PortionModel>>> portionsResponse =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<Integer, List<PortionModel>>>() {}
                );

        assertThat(portionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<Integer, List<PortionModel>> portions = portionsResponse.getBody();
        logger.info("portionsResponse.getBody() is {}", portions);
        assertThat(portions).isNotNull();

        // Step 3: Calculate nutrition

        List<IngredientSelectionModel> selections = createSelections(portions, ingredientToIdMap);

        HttpEntity<List<IngredientSelectionModel>> requestEntity =
                new HttpEntity<>(selections, new HttpHeaders());

        ResponseEntity<RecipeNutritionModel> nutritionResponse =
                restTemplate.exchange(
                        "/api/recipes/calculate-nutrition?servings=4",
                        HttpMethod.PUT,
                        requestEntity,
                        RecipeNutritionModel.class
                );
        assertThat(nutritionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        RecipeNutritionModel nutrition = nutritionResponse.getBody();
        assertThat(nutrition).isNotNull();

        assertThat(nutrition.getServings()).isEqualTo(4);

        NutritionValuesModel totalNutrition = nutrition.getTotalNutrition();
        assertThat(totalNutrition).isNotNull();
        assertThat(totalNutrition.getCalories()).isNotNull();
        assertThat(totalNutrition.getProtein()).isNotNull();
        assertThat(totalNutrition.getFat()).isNotNull();
        assertThat(totalNutrition.getCarbs()).isNotNull();
        assertThat(totalNutrition.getFiber()).isNotNull();
        assertThat(totalNutrition.getTotalSugar()).isNotNull();
        assertThat(totalNutrition.getSatFat()).isNotNull();
        assertThat(totalNutrition.getCholesterol()).isNotNull();
        assertThat(totalNutrition.getSodium()).isNotNull();

        NutritionValuesModel perServingNutrition = nutrition.getPerServingNutrition();
        assertThat(perServingNutrition).isNotNull();
        assertThat(perServingNutrition.getCalories()).isNotNull();
        assertThat(perServingNutrition.getProtein()).isNotNull();
        assertThat(perServingNutrition.getFat()).isNotNull();
        assertThat(perServingNutrition.getCarbs()).isNotNull();
        assertThat(perServingNutrition.getFiber()).isNotNull();
        assertThat(perServingNutrition.getTotalSugar()).isNotNull();
        assertThat(perServingNutrition.getSatFat()).isNotNull();
        assertThat(perServingNutrition.getCholesterol()).isNotNull();
        assertThat(perServingNutrition.getSodium()).isNotNull();

        assertThat(perServingNutrition.getCalories()).isEqualTo(totalNutrition.getCalories() / nutrition.getServings());
        assertThat(perServingNutrition.getProtein()).isEqualTo(totalNutrition.getProtein() / nutrition.getServings());
        assertThat(perServingNutrition.getFat()).isEqualTo(totalNutrition.getFat() / nutrition.getServings());
        assertThat(perServingNutrition.getCarbs()).isEqualTo(totalNutrition.getCarbs() / nutrition.getServings());
    }


    private List<IngredientSelectionModel> createSelections(Map<Integer, List<PortionModel>> portions,
                                                            Map<String, Integer> ingredientToIdMap) {
        Pattern WEIGHT_PATTERN =
                Pattern.compile("(g|gram|grams|oz|ounce|ounces|lb|lbs|pound|pounds|kg|kilogram|kilograms)");

        List<IngredientSelectionModel> selections = new ArrayList<>();

        RecipeModel recipe = recipeRepository.getRecipeById(recipeId);

        String[] ingredients = recipe.getIngredients().split("\n");
        // The map we are adding to here makes it easy to reference the parsed ingredient
        // when we are looping through the portions map by ingredient name below
        for (String ingredientLine : ingredients) {
            IngredientModel parsedIngredient = ingredientParserService.parse(ingredientLine);
            logger.info("parsedIngredient is {}", parsedIngredient);
            IngredientSelectionModel selection = new IngredientSelectionModel();

            String originalUnit = parsedIngredient.getUnit();
            Matcher matcher = null;
            if (originalUnit != null) {
                matcher = WEIGHT_PATTERN.matcher(parsedIngredient.getUnit());
            }

            Integer fdcId = ingredientToIdMap.get(parsedIngredient.getMainIngredient());
            // If the unit for this ingredient is already a weight
            if (matcher != null && matcher.matches()) {
                logger.info("Unit was a weight");
                selection.setPortionId(null);
                selection.setFdcId(fdcId);
                selection.setQuantity(parsedIngredient.getAmount());
                selection.setUnit(parsedIngredient.getUnit());
            } else if (matcher == null || !matcher.matches()) {
                // Else if ingredient unit is not a weight, choose the first portion size
                // arbitrarily for testing simplicity
                logger.info("Unit was not a weight");
                PortionModel zeroethPortion = portions.get(fdcId).get(0);
                selection.setPortionId(zeroethPortion.getPortionId());
                selection.setFdcId(zeroethPortion.getFdcId());
                // Arbitrary quantity for testing simplicity
                selection.setQuantity(1.0);
                selection.setUnit(zeroethPortion.getUnit());
            }
            logger.info("New selection is {}", selection.toString());
            selections.add(selection);
        }

        return selections;
    }


    /**
     * Simulates the selection of elements from the matches list by choosing the zeroeth
     * element out of the potential matches for each ingredient, i.e. the element with
     * the highest confidence
     */
    private Map<String, Integer> makeIngredientToIdMap(Map<String, List<IngredientMatchModel>> matches) {
        Map<String, Integer> ingredientToIdMap = new HashMap<>();

        for (String ingredient : matches.keySet()) {
            IngredientMatchModel zeroethMatch = matches.get(ingredient).get(0);
            ingredientToIdMap.put(ingredient, zeroethMatch.getFdcId());
        }

        return ingredientToIdMap;
    }
}
