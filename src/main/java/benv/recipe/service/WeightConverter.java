package benv.recipe.service;

import benv.recipe.model.IngredientSelectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WeightConverter {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(IngredientMatchService.class);

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

    public WeightConverter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("(g|gram|grams|oz|ounce|ounces|lb|lbs|pound|pounds|kg|kilogram|kilograms)");

    public Double convertToGrams(IngredientSelectionModel selection) {
        String unit = selection.getUnit().trim();

        // If the selection object has no portion ID, the unit must be a weight
        if (selection.getPortionId() == null) {
            Matcher matcher = WEIGHT_PATTERN.matcher(unit);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid weight format: " + unit);
            }

            Double gramsPerUnit = gramsPerUnits.get(unit);
            Double gramsQuantity = selection.getQuantity() * gramsPerUnit;
            logger.info("Weight given, converted to {} grams", gramsQuantity);

            return gramsQuantity;
        } else {
            double gramsPerPortion = jdbcTemplate.queryForObject(
                    "SELECT name FROM my_table WHERE portion_id = ?",
                    Double.class,
                    selection.getPortionId()
            );
            double gramsTotal = gramsPerPortion * selection.getQuantity();
            return gramsTotal;
        }

    }

}
