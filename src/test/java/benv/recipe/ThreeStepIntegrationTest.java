package benv.recipe;

import benv.recipe.model.*;
import benv.recipe.repository.RecipeRepository;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThreeStepIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

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

        List<Integer> selectedFdcIds = extractSelectedFdcIds(matches);
        assertThat(selectedFdcIds).isNotEmpty();

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/recipes/portions");
        // Add (possibly) multiple query parameters with name `fdcIds`, one for the
        // fdcId of each ingredient we've chosen to get portions of. These are converted
        // by the controller to a List in java
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

        List<IngredientSelectionModel> selections = createSelections(portions);
    }


    private List<IngredientSelectionModel> createSelections(Map<String, List<PortionModel>> portions) {
        Pattern WEIGHT_PATTERN =
                Pattern.compile("(g|gram|grams|oz|ounce|ounces|lb|lbs|pound|pounds|kg|kilogram|kilograms)");

        List<IngredientSelectionModel> selections = new ArrayList<>();

        for (List<PortionModel> portionList : portions.values()) {
            IngredientSelectionModel selection = new IngredientSelectionModel();


        }
    }


    /**
     * Simulates the selection of elements from the matches list by choosing the zeroeth
     * element, i.e. the element with the highest confidence
     */
    private List<Integer> extractSelectedFdcIds(Map<String, List<IngredientMatchModel>> matches) {
        List<Integer> fdcIds = new ArrayList<>();

        for (List<IngredientMatchModel> matchList: matches.values()) {
            if (!matchList.isEmpty()) {
                fdcIds.add(matchList.get(0).getFdcId());
            }
        }

        return fdcIds;
    }
}
