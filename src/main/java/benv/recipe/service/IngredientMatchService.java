package benv.recipe.service;

import benv.recipe.model.IngredientMatchModel;
import benv.recipe.model.IngredientModel;
import benv.recipe.model.RecipeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.Math.max;

@Service
public class IngredientMatchService {
    int MATCH_COUNT = 20;
    double STANDARD_WEIGHT = 1.0;
    double QUALIFIER_WEIGHT = 0.5;
    double COMMON_WORD_WEIGHT = 0.2;
    Set<String> qualifiers = new HashSet<>(Arrays.asList(
            "dried", "fresh", "frozen", "canned", "sliced", "diced", "chopped",
            "minced", "grated", "whole", "ground", "crushed", "peeled", "raw",
            "cooked", "boiled", "roasted", "baked", "fried", "steamed", "sweet",
            "sour", "bitter", "spicy", "hot", "cold", "warm", "ripe", "unripe",
            "stemmed", "cored", "red", "orange", "yellow", "green", "blue", "purple",
            "pink", "white", "black", "brown", "gray"
    ));
    Set<String> grammar = new HashSet<>(Arrays.asList(
                "a", "an", "the", "in", "on", "at", "by", "for", "with", "from", "of", "to",
                "and", "but", "or", "nor", "so", "yet", "as", "because", "if", "when", "while"
    ));
    Set<String> commonWords = new HashSet<>(Arrays.asList(
            "oil", "flour", "sugar", "salt", "water", "sauce", "powder",
            "fresh", "dried", "milk", "cream", "juice", "extract"
    ));


    private final JdbcTemplate jdbcTemplate;
    private final IngredientParserService ingredientParserService;
    private final Comparator<IngredientMatchModel> scoreComparator =
            Comparator.comparingDouble(IngredientMatchModel::getConfidence);
    private static final Logger logger = LoggerFactory.getLogger(IngredientMatchService.class);


    @Autowired
    private IngredientMatchService(JdbcTemplate jdbcTemplate,
                                   IngredientParserService ingredientParserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.ingredientParserService = ingredientParserService;
    }

    public Map<String, List<IngredientMatchModel>> fetchMatches(RecipeModel recipe) {

        String[] ingredients = recipe.getIngredients().split("\n");
//        System.out.printf("Array of ingredients split at newlines is %s%n", Arrays.toString(ingredients));
        Map<String, List<IngredientMatchModel>> matchesContainer = new HashMap<>();

        for (String ingredientLine : ingredients) {
            // System.out.printf("ingredientLine is %s%n", ingredientLine);
            IngredientModel searchModel =  ingredientParserService.parse(ingredientLine);
            logger.info("searchModel is {}", searchModel.toString());
            PriorityQueue<IngredientMatchModel> matchQueue = new PriorityQueue<>(scoreComparator);

            singleMatches(searchModel, matchQueue);

            List<IngredientMatchModel> matchList = new ArrayList<>(matchQueue);
            // TODO Sort tertiarily by number of characters in name
            matchList.sort(Comparator.comparing(IngredientMatchModel::getConfidence).reversed()
                    .thenComparing(match -> match.getName().split("\\s+").length));
            matchesContainer.put(searchModel.getMainIngredient(), matchList);
        }

        return matchesContainer;
    }

    public void singleMatches(IngredientModel searchModel,
                              PriorityQueue<IngredientMatchModel> matchQueue) {

        String searchTerm = searchModel.getMainIngredient().trim().toLowerCase();

        findExactMatches(searchTerm, matchQueue);

        logger.info("Before check that matchQueue.size() < 5, matchQueue is {}", matchQueue);
        if (matchQueue.size() < MATCH_COUNT) {
            List<Map<String, Object>> candidates = new ArrayList<>();
            candidates = reduceCandidates(searchTerm, candidates);

            System.out.println("Candidates list is:");
            for (Map<String, Object> candidate : candidates) {
                System.out.println(candidate);
            }

            tokenSearch(searchTerm, candidates, matchQueue);
        }
    }

    private void findExactMatches(String searchTerm, PriorityQueue<IngredientMatchModel> matchQueue) {
        String sql = "SELECT fdc_id, name FROM ingredients WHERE LOWER(name) = ?";
        List<Map<String, Object>> exactMatches = jdbcTemplate.queryForList(sql, searchTerm);
        // logger.info("In findExactMatches");

        if (!exactMatches.isEmpty()){
            for (Map<String, Object> exactMatch : exactMatches ) {
                // logger.info("Exact match is {} for searchTerm {}", exactMatch, searchTerm);
                IngredientMatchModel newMatch = new IngredientMatchModel();
                // Need to cast here because queryForList() returns a map with values that are simply Objects
                newMatch.setFdcId((Integer) exactMatch.get("fdc_id"));
                newMatch.setName((String) exactMatch.get("name"));
                newMatch.setConfidence(1.0);
                matchQueue.offer(newMatch);
            }
        }
    }

    private List<Map<String, Object>> reduceCandidates(String searchTerm,
                                                       List<Map<String, Object>> candidates) {
        List<String> ingredientWords = List.of(searchTerm.split("\\s+"));
        List<String> mainIngredients = new ArrayList<>();
        for (String word : ingredientWords) {
            if (!qualifiers.contains(word) && !grammar.contains(word)) {
                mainIngredients.add(word);
            }
        }

        List<String> mainIngredientVariations = new ArrayList<>(mainIngredients);

        // Add all possible variations of the main ingredients to the mainIngredients list
        for (String mainIngredient : mainIngredients) {
            if (mainIngredient.endsWith("ies")) {
                mainIngredientVariations.add(mainIngredient.substring(0, mainIngredient.length() - 3) + "y");
            }
            else if (mainIngredient.endsWith("es")) {
                mainIngredientVariations.add(mainIngredient.substring(0, mainIngredient.length() - 2));
            }
            else if (mainIngredient.endsWith("s")) {
                mainIngredientVariations.add(mainIngredient.substring(0, mainIngredient.length() - 1));
            }
        }

         logger.info("mainIngredientVariations are {}", mainIngredientVariations);

        StringBuilder sql = new StringBuilder("SELECT fdc_id, name FROM ingredients WHERE ");
        List<String> conditions = new ArrayList<>();
        List<String> params = new ArrayList<>();

        for (String mainIngredientVariation : mainIngredientVariations) {
            if (mainIngredientVariation.length() >= 3) {
                conditions.add("LOWER(name) LIKE ?");
                params.add("%" + mainIngredientVariation + "%");
            }
        }

        sql.append(String.join(" OR ", conditions));

        logger.info("In reduceCandidates, sql is {}", sql);
        logger.info("In reduced candidates, params is {}", params);
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public void tokenSearch (String searchTerm, List<Map<String,
            Object>> dbCandidates, PriorityQueue<IngredientMatchModel> matchQueue ) {

        logger.info("In tokenSearch, searchTerm is {}", searchTerm);

        String[] searchSplit = searchTerm.split("\\s+");
        List<String> searchTokens = new ArrayList<>();
        for (String splitWord : searchSplit) {
            String cleanWord = splitWord.replaceAll("[,()]", "").toLowerCase();
            if (!grammar.contains(cleanWord)) {
                searchTokens.add(cleanWord);
            }
        }

        for (Map<String, Object> dbCandidate : dbCandidates) {
            logger.info("dbCandidate is {}", dbCandidate);
            Integer fdcId = (Integer) dbCandidate.get("fdc_id");
            String name = (String) dbCandidate.get("name");

            String[] dbCandidateSplit = name.split("\\s+");
            List<String> dbCandidateTokens = new ArrayList<>();
            for (String splitWord : dbCandidateSplit) {
                String cleanWord = splitWord.replaceAll("[,()]", "").toLowerCase();
                if (!grammar.contains(cleanWord)) {
                    dbCandidateTokens.add(cleanWord);
                }
            }

            double totalScore = 0;

            for (String searchToken : searchTokens) {
                // logger.info("searchToken is {}", searchToken);
                double tokenWeight;
                // Get lower score for matching with a qualifier than part of main ingredient
                if (qualifiers.contains(searchToken)) {
                    tokenWeight = QUALIFIER_WEIGHT;
                } else if (commonWords.contains(searchToken)) {
                    tokenWeight = COMMON_WORD_WEIGHT;
                } else {
                    tokenWeight = STANDARD_WEIGHT;
                }

                // This is unique to each search token, represents closest match between search
                // token and any db token.
                double bestMatchScore = 0.0;

                // This is the first of two identical loops since it's more efficient to check all
                // db tokens for exact matches first before performing Levenshtein distance
                for (String dbToken : dbCandidateTokens) {
                    if (searchToken.equals(dbToken)) {
                        logger.info("Found exact match with searchToken {} and dbToken {}", searchToken, dbToken);
                        bestMatchScore = 1.0;
                        break;
                    }
                }

                // If we haven't yet found a perfect match for the searchToken, we enter this
                // if statement and check for similarity via Levenshtein distance
                if (bestMatchScore != 1.0) {
                    for (String dbToken : dbCandidateTokens) {
                        dbToken = dbToken.toLowerCase();
                        // logger.info("dbToken is {} in Levenshtein loop", dbToken);
                        if (searchToken.length() >= 3 && dbToken.length() >= 3) {
                            double similarity = normalizedLevSimilarity(searchToken, dbToken);
                            logger.info("Similarity score for search token {} and db " +
                                     "token {} is {}", searchToken, dbToken, similarity);
                            bestMatchScore = max(bestMatchScore, similarity);

                        }
                    }
                }
                // If the searchToken matched pretty well with one of the dbTokens, it
                // contributes positively to the score.
                if (bestMatchScore >= 0.7) {
                    totalScore += bestMatchScore * tokenWeight;
                    logger.info("bestMatchScore is {} for searchToken {}, setting total score " +
                                    "to {}", bestMatchScore, searchToken, totalScore);
                }
            }


            // Score is count of number of tokens, where qualifiers count as 0.5.
            double maxTokenCount = max(calculateTokenSum(searchTokens),
                                       calculateTokenSum(dbCandidateTokens));
            logger.info("maxTokenCount is {}", maxTokenCount);
            double normalizedScore = totalScore / maxTokenCount;
            if (normalizedScore > 0) {
                normalizedScore = modifyScore(normalizedScore, searchTokens, dbCandidateTokens);
            }
            IngredientMatchModel match = new IngredientMatchModel();
            match.setFdcId(fdcId);
            match.setName(name);
            match.setConfidence(normalizedScore);

            addToLimitedQueue(matchQueue, match);
        }
    }

    private void addToLimitedQueue(
            PriorityQueue<IngredientMatchModel> matchQueue,
            IngredientMatchModel item) {

        // Prevents duplicate entries into queue
        IngredientMatchModel[] matchArray = matchQueue.toArray(new IngredientMatchModel[0]);
        for (IngredientMatchModel match: matchArray) {
            if (item.getFdcId().equals(match.getFdcId())) {
                return;
            }
        }

        if (matchQueue.size() < MATCH_COUNT) {
            matchQueue.add(item);
            logger.info("matchQueue updated, added item with name {}", item.getName());
        }
        else {
            IngredientMatchModel lowest = matchQueue.peek();
            if (lowest.getConfidence() < item.getConfidence()) {
                IngredientMatchModel deleted = matchQueue.poll();
                matchQueue.offer(item);
                logger.info("matchQueue updated, deleted element with name {} and added element with name {}",
                        deleted.getName(), item.getName());
            } else if (lowest.getConfidence() == item.getConfidence()) {
                List<IngredientMatchModel> tiedElements = new ArrayList<>();

                // Collect all elements with the same lowest confidence
                for (IngredientMatchModel match : matchQueue) {
                    if (match.getConfidence() == lowest.getConfidence()) {
                        tiedElements.add(match);
                    }
                }

                IngredientMatchModel elementToRemove = tiedElements.stream()
                        .max(Comparator.comparingInt(m -> m.getName().split("\\s+").length))
                        .orElse(lowest);

                matchQueue.remove(elementToRemove);
                matchQueue.offer(item);
                logger.info("Tie handling: removed {} ({} words) and added {}",
                        elementToRemove.getName(),
                        elementToRemove.getName().split("\\s+").length,
                        item.getName());
            }
        }

        System.out.println("\nQueue contents using enhanced for-loop:");
        for (IngredientMatchModel element : matchQueue) {
            System.out.println("- " + element.getName());
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

        int maxLength = max(searchToken.length(), dbToken.length());

        double normalizedSimilarity = 1.0 - ((double) distance[searchToken.length()][dbToken.length()] / maxLength);

        return normalizedSimilarity;
    }

    /**
     * This function calculates a value that is used to normalize the Levenshtein similarity
     * scores via division. This value is basically just the count of the number of terms in
     * tokenList, but with qualifiers giving only half a point to the count.
     *
     * @param tokenList
     * @return score
     */
    private double calculateTokenSum(List<String> tokenList) {
        double score = 0.0;
        logger.info("tokenList is {}", tokenList);
        for (String term : tokenList) {
            logger.info("Token sum is {}, term is {}", score, term);
            if (grammar.contains(term)) {
                logger.info("Found grammar word {}", term);
                continue;
            }
            else if (commonWords.contains(term) ) {
                score += COMMON_WORD_WEIGHT;
            }
            else if (qualifiers.contains(term) ) {
                score += QUALIFIER_WEIGHT;
            } else {
                score += STANDARD_WEIGHT;
            }
        }
        logger.info("Final token sum is {}", score);
        return score;
    }

    /**
     * This function adds weight to database candidates that are in unprocessed form and
     * removes weight for processed forms, _if_ the user isn't searching for a processed form.
     *
     */
    private double modifyScore(double normalizedScore, List<String> searchTokens,
                               List<String> dbCandidateTokens) {
        Set<String> rawIndicators = new HashSet<>(Arrays.asList(
                "raw", "fresh", "whole", "unprepared"
        ));
        Set<String> processedIndicators = new HashSet<>(Arrays.asList(
                "flour", "powder", "extract", "juice", "chips", "sauce", "oil",
                "frozen", "canned", "dried", "salted", "pickled", "dressing", "puree",
                "paste"
        ));
        double modifiedScore = normalizedScore;
        logger.info("modifiedScore = normalizedScore = {} at start of modifyScore", modifiedScore);
       
        
        // Determine if there's a token from processedIndicators in the searchTerm
        boolean processedInSearchTerm = false;
        for (String searchToken : searchTokens) {
            if (processedIndicators.contains(searchToken)) {
                processedInSearchTerm = true;
                break;
            }
        }


        for (String dbToken: dbCandidateTokens) {
            // If the db term is a raw item
            if (rawIndicators.contains(dbToken)) {
                // And we aren't searching for a processed item, boost the score
                if (!processedInSearchTerm) {
                    modifiedScore += 0.15;
                    logger.info("Token {} boosts score to {}", dbToken, modifiedScore);
                }
                break;
            }
        }

        boolean containsProcessedIndicator = false;
        for (String token : dbCandidateTokens) {
            // If the db term is a processed item
            if (processedIndicators.contains(token)) {
                // Unless we're looking for a processed item, subtract from score
                if (!processedInSearchTerm) {
                    modifiedScore -= 0.15;
                    logger.info("Token {} reduces score to {}", token, modifiedScore);
                }
                break;
            }
        }
        

        // Make sure score stays between 0 and 1
        return Math.min(1.0, Math.max(0, modifiedScore));
    }
}
