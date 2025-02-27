package benv.recipe.service;

import benv.recipe.model.IngredientMatchModel;
import benv.recipe.model.IngredientModel;
import benv.recipe.model.RecipeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IngredientMatchService {
    private final JdbcTemplate jdbcTemplate;
    private final IngredientParserService ingredientParserService;

    @Autowired
    private IngredientMatchService(JdbcTemplate jdbcTemplate,
                                   IngredientParserService ingredientParserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.ingredientParserService = ingredientParserService;
    }

    public List<IngredientMatchModel> fetchMatches(RecipeModel recipe) {

        String[] ingredients = recipe.getIngredients().split("\n");

        for (String ingredientLine : ingredients) {
            IngredientModel ingredientModel =  ingredientParserService.parse(ingredientLine);

            singleMatches(ingredientModel);
        }
    }

    public List<IngredientMatchModel> singleMatches(IngredientModel ingredientModel) {
        List<IngredientMatchModel> matches = new ArrayList<>();

        String searchTerm = ingredientModel.getIngredient().trim().toLowerCase();

        findExactMatches(searchTerm, matches);

        if (matches.size() < 5) {
            findWordMatches(searchTerm, matches);
        }

        return matches;
    }

    private void findExactMatches(String searchTerm, List<IngredientMatchModel> matches) {
        String sql = "SELECT fdc_id, name FROM ingredients WHERE LOWER(name) = ?";
        List<Map<String, Object>> exactMatches = jdbcTemplate.queryForList(sql, searchTerm);

        if (!exactMatches.isEmpty()){
            for (Map<String, Object> exactMatch : exactMatches ) {
                IngredientMatchModel newMatch = new IngredientMatchModel();
                // Need to cast here because queryForList() returns a map with values that are simply Objects
                newMatch.setFdcId((Integer) exactMatch.get("fdc_id"));
                newMatch.setName((String) exactMatch.get("name"));
                newMatch.setConfidence(1.0);
                matches.add(newMatch);
            }
        }
    }

    private void findWordMatches(String searchTerm, List<IngredientMatchModel> matches) {
        String[] words = searchTerm.split("\\s+");
        Set<String> qualifiers = new HashSet<>(Arrays.asList(
                "dried", "fresh", "frozen", "canned", "sliced", "diced", "chopped",
                "minced", "grated", "whole", "ground", "crushed", "peeled", "raw",
                "cooked", "boiled", "roasted", "baked", "fried", "steamed", "sweet",
                "sour", "bitter", "spicy", "hot", "cold", "warm", "ripe", "unripe",
                "stemmed", "cored"
        ));




    }
}
