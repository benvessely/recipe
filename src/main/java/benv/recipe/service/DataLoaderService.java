package benv.recipe.service;

import benv.recipe.model.RecipeModel;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.springframework.core.io.ClassPathResource;


@Service
public class DataLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(DataLoaderService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DataLoaderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadDataOnStartup() {
        resetTables();
        logger.info("Starting USDA data load");
        loadFoodData();
        loadNutrientData();
        loadPortionData();
        logger.info("Completed USDA data load");
    }

    @Transactional
    public void resetTables() {
        try {
            logger.info("Dropping ingredients and portions tables...");
            jdbcTemplate.execute("DROP TABLE IF EXISTS ingredients CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS portions CASCADE");

            logger.info("Creating tables from schema...");
            // Read and execute the schema.sql file
            String schemaSql = new String(new ClassPathResource("schema.sql")
                    .getInputStream().readAllBytes());
            jdbcTemplate.execute(schemaSql);
        } catch (IOException e) {
            logger.error("Failed to read schema.sql file in resetTables()", e);
            throw new RuntimeException("Failed to initialize database schema in resetTables()", e);
        }
    }

    public void loadFoodData() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("nutrition_data/food.csv").getInputStream()))) {

            String[] headers = reader.readNext();
            System.out.println(Arrays.toString(headers)); //DB

            logger.info("Start loading basic data from food.csv");
            String[] row;
            int rowNum = 0;
            while ((row = reader.readNext()) != null) {
                rowNum += 1;
                int fdcId = Integer.parseInt(row[0]);
                String name = row[2];
                int categoryId = Integer.parseInt(row[3]);

                String sql = """
                    INSERT INTO ingredients (fdc_id, name, category_id)
                    VALUES (?, ?, ?)
                    """;
                jdbcTemplate.update(sql, fdcId, name, categoryId);
            }
            logger.info("Went through {} rows in food.csv", rowNum);
        } catch (IOException e) {
            logger.error("Could not read CSV file", e);
            throw new RuntimeException("Failed to read CSV file", e);
        } catch (CsvValidationException e) {
            logger.error("Could not validate CSV", e);
            throw new RuntimeException("Failed to parse CSV", e);
        }
    }

    private void loadNutrientData() {
        Map<String, String> nutrientIdToColumn = createNutrientMap();

        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("nutrition_data/food_nutrient.csv").getInputStream()))) {

            String[] headers = reader.readNext();
            String[] row;

            logger.info("Start loading nutrient data");
            int rowNum = 0;
            while ((row = reader.readNext()) != null) {
//                logger.info("row = {} in loadNutrientData");A
                rowNum += 1;
                Integer fdcId = Integer.parseInt(row[1]);
                String nutrientId = row[2];
                Double amount = Double.parseDouble(row[3]);

                if (nutrientIdToColumn.containsKey(nutrientId)) {
                    String column = nutrientIdToColumn.get(nutrientId);
                    jdbcTemplate.update(
                            String.format("UPDATE ingredients SET %s = ? WHERE fdc_id = ?", column),
                            amount, fdcId
                    );
                }
            }
            logger.info("Went through {} rows in the food_nutrient.csv table", rowNum);
        } catch (IOException e) {
            logger.error("Could not read CSV file", e);
            throw new RuntimeException("Failed to read CSV file", e);
        } catch (CsvValidationException e) {
            logger.error("Could not validate CSV", e);
            throw new RuntimeException("Failed to parse CSV", e);
        }
    }

    private static Map<String, String> createNutrientMap() {
        Map<String, String> nutrientIdToColumn = new HashMap<>();
        nutrientIdToColumn.put("1008", "calories_per_100g");
        nutrientIdToColumn.put("1003", "protein_per_100g");
        nutrientIdToColumn.put("1004", "fat_per_100g");
        nutrientIdToColumn.put("1005", "carbs_per_100g");
        nutrientIdToColumn.put("1079", "fiber_per_100g");
        nutrientIdToColumn.put("2000", "total_sugar_per_100g");
        nutrientIdToColumn.put("1258", "sat_fat_per_100g");
        nutrientIdToColumn.put("1253", "cholesterol_per_100g");
        nutrientIdToColumn.put("1093", "sodium_per_100g");
        return nutrientIdToColumn;
    }

    private void loadPortionData() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("nutrition_data/food_portion.csv").getInputStream()))) {

            String[] headers = reader.readNext();
            String[] row;

            while ((row = reader.readNext()) != null) {
                Integer fdcId = Integer.parseInt(row[1]);
                Double amount = Double.parseDouble(row[3]);
                String modifier = row[6];
                Double weight = Double.parseDouble(row[7]);

                jdbcTemplate.update(
                        "INSERT INTO portions(fdc_id, amount, modifier, weight) " +
                                "VALUES (?, ?, ?, ?)",
                        fdcId, amount, modifier, weight
                );
            }
        } catch (IOException e) {
            logger.error("Could not read CSV file", e);
            throw new RuntimeException("Failed to read CSV file", e);
        } catch (CsvValidationException e) {
            logger.error("Could not validate CSV", e);
            throw new RuntimeException("Failed to parse CSV", e);
        }
    }
}