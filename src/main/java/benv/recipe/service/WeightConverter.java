package benv.recipe.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WeightConverter {

    // Written so that one unit of the {key} is {value} number of grams
    static Map<String, Double> gramsPerUnits = new HashMap<>();

    static {
        gramsPerUnits.put("g", 1.0);
        gramsPerUnits.put("gram", 1.0);
        gramsPerUnits.put("grams", 1.0);

        gramsPerUnits.put("oz", 28.35);
        gramsPerUnits.put("ounce", 28.35);
        gramsPerUnits.put("ounces", 28.35);

        gramsPerUnits.put("lb", 453.59);
        gramsPerUnits.put("lbs", 453.59);
        gramsPerUnits.put("pound", 453.59);
        gramsPerUnits.put("pounds", 453.59);

        gramsPerUnits.put("kg", 1000.0);
        gramsPerUnits.put("kgs", 1000.0);
        gramsPerUnits.put("kilogram", 1000.0);
        gramsPerUnits.put("kilograms", 1000.0);
    }

    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("(g|gram|grams|oz|ounce|ounces|lb|lbs|pound|pounds|kg|kilogram|kilograms)");

    public Double convertToGrams(Double quantity, String unit) {
        Matcher matcher = WEIGHT_PATTERN.matcher(unit);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid weight format: " + unit);
        }

        Double gramsPerUnit = gramsPerUnits.get(unit);
        Double gramsQuantity = quantity * gramsPerUnit;

        return gramsQuantity;
    }


}
