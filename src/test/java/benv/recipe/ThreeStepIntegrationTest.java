package benv.recipe;

import benv.recipe.model.*;
import benv.recipe.repository.RecipeRepository;
import benv.recipe.service.IngredientParserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        testRecipe.setIngredients("2 lb beef chuck roast\n1 cup red wine\n4 carrots");
        testRecipe.setInstructions("Cook food");
        RecipeModel createdRecipe = recipeRepository.createRecipe(testRecipe);
        recipeId = createdRecipe.getId();
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

        ResponseEntity<Map<String, List<PortionModel>>> portionsResponse =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, List<PortionModel>>>() {}
                );

        assertThat(portionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, List<PortionModel>> portions = portionsResponse.getBody();
        assertThat(portions).isNotNull();

        // Step 3: Calculate nutrition

        List<IngredientSelectionModel> selections = createSelections(portions, ingredientToIdMap);

    }


    private List<IngredientSelectionModel> createSelections(Map<String, List<PortionModel>> portions,
                                                            Map<String, Integer> ingredientToIdMap) {
        Pattern WEIGHT_PATTERN =
                Pattern.compile("(g|gram|grams|oz|ounce|ounces|lb|lbs|pound|pounds|kg|kilogram|kilograms)");

        List<IngredientSelectionModel> selections = new ArrayList<>();

        Map<String, IngredientModel> parsedMap = new HashMap<>();
        RecipeModel recipe = recipeRepository.getRecipeById(recipeId);

        String[] ingredients = recipe.getIngredients().split("\n");
        // The map we are adding to here makes it easy to reference the parsed ingredient
        // when we are looping through the portions map by ingredient name below
        for (String ingredientLine : ingredients) {
            IngredientModel parsedIngredient = ingredientParserService.parse(ingredientLine);
            parsedMap.put(parsedIngredient.getMainIngredient(), parsedIngredient);
        }

        for (String ingredientName : portions.keySet()) {
            IngredientSelectionModel selection = new IngredientSelectionModel();
            IngredientModel parsedIngredient = parsedMap.get(ingredientName);

            Matcher matcher = WEIGHT_PATTERN.matcher(parsedIngredient.getUnit());

            // If the unit for this ingredient is already a weight
            if (matcher.matches()) {
                selection.setPortionId(null);
                Integer fdcId = ingredientToIdMap.get(ingredientName);
                selection.setFdcId(fdcId);
                selection.setQuantity(parsedIngredient.getAmount());
                selection.setUnit(parsedIngredient.getUnit());
            } else {
                // Else if ingredient unit is not a weight, choose the first portion size
                // arbitrarily for testing simplicity
                PortionModel zeroethPortion = portions.get(ingredientName).get(0);
                selection.setPortionId(zeroethPortion.getPortionId());
                selection.setFdcId(zeroethPortion.getFdcId());
                selection.setQuantity(1.0);
                selection.setUnit(zeroethPortion.getUnit());
            }
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
