package benv.recipe.service;

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
import java.util.ArrayList;
import java.util.Arrays;

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
        if (isDatabaseEmpty()) {
            logger.info("Starting USDA data load");
            loadFoodData();
            logger.info("Completed USDA data load");
        }
    }

    private boolean isDatabaseEmpty() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ingredients", Integer.class);
        return count != null && count == 0;
    }

    public void loadFoodData() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("nutrition_data/food.csv").getInputStream()))) {
            String[] headers = reader.readNext();
            System.out.println(Arrays.toString(headers)); //DB

            String[] row;
            int i=0; //DB
            while ((row = reader.readNext()) != null && i <= 10) {
                System.out.println(Arrays.toString(row)); //DB
                i++;
                int fdcId = Integer.parseInt(row[0]);
                String description = row[2];
                int category_id = Integer.parseInt(row[3]);
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