package benv.recipe.service;

import benv.recipe.controller.RecipeController;
import benv.recipe.model.PortionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PortionService {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PortionService.class);

    private static class PortionRowMapper implements RowMapper<PortionModel> {
        @Override
        public PortionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            PortionModel portion = new PortionModel();
            portion.setPortionId(rs.getInt("portion_id"));
            portion.setFdcId(rs.getInt("fdc_id"));
            portion.setUnitAmount(rs.getDouble("amount"));
            portion.setUnit(rs.getString("modifier"));
            portion.setWeightPer100g(rs.getDouble("weight"));
            return portion;
        }
    }

    private final RowMapper<PortionModel> portionRowMapper = new PortionRowMapper();

    @Autowired
    public PortionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer, List<PortionModel>> getPortions(List<Integer> fdcIds) {
        Map<Integer, List<PortionModel>> portionMap = new HashMap<>();

        for (Integer fdcId : fdcIds) {
            List<PortionModel> portionsList = getPortionsByFdcId(fdcId);
            portionMap.put(fdcId, portionsList);
        }

        return portionMap;
    }

    public List<PortionModel> getPortionsByFdcId(Integer fdcId) {
        return jdbcTemplate.query(
                "SELECT * FROM portions WHERE fdc_id = ?",
                portionRowMapper,
                fdcId
        );
    }

    private Map<Integer, String> getIngredientNames(List<Integer> fdcIds) {
        String fdcIdsStr = fdcIds.stream()
                .map(id -> String.valueOf(id))
                .collect(Collectors.joining(","));

        String sql = "SELECT fdc_id, name FROM ingredients WHERE fdc_id IN (" +
                fdcIdsStr + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<Integer, String> result = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer fdcId = (Integer) row.get("fdc_id");
            String name = (String) row.get("name");
            result.put(fdcId, name);
        }

        return result;
    }
}