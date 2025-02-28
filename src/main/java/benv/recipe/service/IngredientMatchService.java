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

    public Map<String, List<IngredientMatchModel>> fetchMatches(RecipeModel recipe) {

        String[] ingredients = recipe.getIngredients().split("\n");
        Map<String, List<IngredientMatchModel>> matchesContainer = new HashMap<>();

        for (String ingredientLine : ingredients) {
            IngredientModel searchModel =  ingredientParserService.parse(ingredientLine);
            PriorityQueue<IngredientMatchModel> matches = new ArrayList<>();

            singleMatches(searchModel, matches);
        }

        return matchesContainer;
    }

    public List<IngredientMatchModel> singleMatches(IngredientModel searchModel, List<IngredientMatchModel> matches) {

        String searchTerm = searchModel.getIngredient().trim().toLowerCase();

        findExactMatches(searchTerm, matches);

        if (matches.size() < 5) {
            List<Map<String, Object>> candidates = new ArrayList<>();
            candidates = reduceCandidates(searchTerm);
            matches = tokenSearch(searchTerm, candidates, matches);
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

    private List<Map<String, Object>> reduceCandidates(String searchTerm) {
        Set<String> qualifiers = new HashSet<>(Arrays.asList(
                "dried", "fresh", "frozen", "canned", "sliced", "diced", "chopped",
                "minced", "grated", "whole", "ground", "crushed", "peeled", "raw",
                "cooked", "boiled", "roasted", "baked", "fried", "steamed", "sweet",
                "sour", "bitter", "spicy", "hot", "cold", "warm", "ripe", "unripe",
                "stemmed", "cored"
        ));

        List<String> ingredientWords = List.of(searchTerm.split("\\s+"));
        List<String> mainIngredients = new ArrayList<>();
        for (String word : ingredientWords) {
            if (!qualifiers.contains(word) && word.length() >= 3) {
                mainIngredients.add(word);
            }
        }

        // Add all possible variations of the main ingredients to the mainIngredients list
        for (String mainIngredient : mainIngredients) {
            if (mainIngredient.endsWith("ies")) {
                mainIngredients.add(mainIngredient.substring(0, mainIngredient.length() - 3) + "y");
            }
            else if (mainIngredient.endsWith("es")) {
                mainIngredients.add(mainIngredient.substring(0, mainIngredient.length() - 2));
            }
            else if (mainIngredient.endsWith("s")) {
                mainIngredients.add(mainIngredient.substring(0, mainIngredient.length() - 1));
            }
        }

        StringBuilder sql = new StringBuilder("SELECT fdc_id, name FROM ingredients WHERE ");
        List<String> conditions = new ArrayList<>();
        List<String> params = new ArrayList<>();

        // Remember that mainIngredients holds variations now
        for (String mainIngredient : mainIngredients) {
            if (mainIngredient.length() >= 3) {
                conditions.add("LOWER(name) LIKE ?");
                params.add("%" + mainIngredient + "%");
            }
        }

        sql.append(String.join(" OR ", conditions));

        List<Map<String, Object>> candidates = jdbcTemplate.queryForList(sql.toString(), params);
        System.out.printf("For main ingredients {}, number of candidates is {}",
                mainIngredients, candidates.size());
    }

    public List<IngredientMatchModel> tokenSearch (String searchTerm, List<Map<String,
            Object>> dbCandidates, List<IngredientMatchModel> matches ) {
        Set<String> qualifiers = new HashSet<>(Arrays.asList(
                "dried", "fresh", "frozen", "canned", "sliced", "diced", "chopped",
                "minced", "grated", "whole", "ground", "crushed", "peeled", "raw",
                "cooked", "boiled", "roasted", "baked", "fried", "steamed", "sweet",
                "sour", "bitter", "spicy", "hot", "cold", "warm", "ripe", "unripe",
                "stemmed", "cored"
        ));


        String[] searchTokens = searchTerm.split("\\s+");
        for (Map<String, Object> dbCandidate : dbCandidates) {
            Integer fdcId = (Integer) dbCandidate.get("fdc_id");
            String name = (String) dbCandidate.get("name");

            String[] dbCandidateTokens = name.split("\\s+");
            double totalScore = 0;
            // Will build the max_score as we go and use it to normalize at the end
            double maxScore = 0;

            for (String searchToken : searchTokens) {
                double tokenWeight;
                // Get lower score for matching with a qualifier than part of main ingredient
                if (qualifiers.contains(searchToken)) {
                    tokenWeight = 0.5;
                } else {
                    tokenWeight = 1.0;
                }
                maxScore += tokenWeight;

                // This is unique to each search token, represents closest match between search
                // token and any db token.
                double bestMatchScore = 0.0;

                // This is the first of two identical loops since it's more efficient to check all
                // db tokens for exact matches first before performing Levenshtein distance
                for (String dbToken : dbCandidateTokens) {
                    if (searchToken.equals(dbToken)) {
                        bestMatchScore = 1.0;
                        break;
                    }
                }

                for (String dbToken : dbCandidateTokens) {
                    if (searchToken.length() >= 3 && dbToken.length() >= 3) {
                        double similarity = normalizedLevSimilarity(searchToken, dbToken);
                        bestMatchScore = Math.max(bestMatchScore, similarity);
                    }
                }

                totalScore += bestMatchScore * tokenWeight;
            }

            if (totalScore > 0.3) {
                IngredientMatchModel match = new IngredientMatchModel;
                match.setFdcId(fdcId);
                match.setName();
            }
        }
    }

    private double normalizedLevSimilarity(String searchToken, String dbToken) {
        int[][] distance = new int[searchToken.length() + 1][dbToken.length() + 1];

        for (int i = 0; i <= searchToken.length(); i++) {
            for (int j = 0; j <= dbToken.length(); j++) {
                if (i == 0) {
                    distance[i][j] = j;
                } else if (j == 0) {
                    distance[i][j] = i;
                } else {
                    distance[i][j] = Math.min(
                            Math.min(
                                distance[i - 1][j] + 1,     // Deletion
                                distance[i][j - 1] + 1),    // Insertion
                           // Substitution or skip
                           distance[i - 1][j - 1] +
                                   (searchToken.charAt(i - 1) == dbToken.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        int maxLength = Math.max(searchToken.length(), dbToken.length());

        double normalizedSimilarity = 1.0 - ((double) distance[searchToken.length()][dbToken.length()] / maxLength);

        return normalizedSimilarity;
    }

}
