package benv.recipe.service;

import benv.recipe.model.IngredientModel;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IngredientParserService {
    // Common cooking units
    private static final Set<String> UNITS = new HashSet<>(Arrays.asList(
            "cup", "cups", "tablespoon", "tablespoons", "tbsp",
            "teaspoon", "teaspoons", "tsp", "pound", "pounds", "lb",
            "ounce", "ounces", "oz", "gram", "grams", "g",
            "kilogram", "kg", "ml", "milliliter", "liter"
    ));

    // Common quantity words
    private static final Set<String> QUANTITY_WORDS = new HashSet<>(Arrays.asList(
            "bunch", "pinch", "handful", "dash", "piece", "slice", "slices",
            "large", "medium", "small", "whole"
    ));

    public IngredientModel parse(String ingredientText) {
        // Clean the input
        String cleaned = ingredientText.toLowerCase().trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[,;()]", "");

        IngredientModel result = new IngredientModel(ingredientText);

        // Objects are passed by reference, so result is modified directly
        parseComponents(cleaned, result);

        return result;
    }

    private void parseComponents(String text, IngredientModel result) {
        String[] words = text.split(" ");
        StringBuilder ingredientBuilder = new StringBuilder();
        boolean foundQuantity = false;

        for (String word : words) {

            Double amount = parseAmount(word);
            if (!foundQuantity && amount != null) {
                result.setAmount(amount);
                foundQuantity = true;
                continue;
            }

            if (result.getUnit() == null &&
                    (UNITS.contains(word) || QUANTITY_WORDS.contains(word))) {
                result.setUnit(word);
                continue;
            }

            // At this point, word is part of main ingredient (not measure)
            if (!ingredientBuilder.isEmpty()) {
                ingredientBuilder.append(" ");
            }
            ingredientBuilder.append(word);
        }

        String ingredientName = ingredientBuilder.toString().trim();
        if (!ingredientName.isEmpty()) {
            result.setIngredient(ingredientName);
        }
    }

    private Double parseAmount(String word) {
        try {
            return Double.parseDouble(word);
        } catch (NumberFormatException e) {
            if (word.contains("/")) {
                String[] fraction = word.split("/");
                if (fraction.length == 2) {
                    try {
                        return Double.parseDouble(fraction[0]) /
                                Double.parseDouble(fraction[1]);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                }
            }
            return null;
        }
    }
}