package benv.recipe.service;

import benv.recipe.model.PortionModel;
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

    private static class PortionRowMapper implements RowMapper<PortionModel> {
        @Override
        public PortionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            PortionModel portion = new PortionModel();
            portion.setId(rs.getInt("id"));
            portion.setFdcId(rs.getInt("fdc_id"));
            portion.setAmount(rs.getDouble("amount"));
            portion.setModifier(rs.getString("modifier"));
            portion.setWeight(rs.getDouble("gram_weight"));
            return portion;
        }
    }

    private final RowMapper<PortionModel> portionRowMapper = new PortionRowMapper();

    @Autowired
    public PortionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, List<PortionModel>> getPortions(List<Integer> fdcIds) {
        Map<String, List<PortionModel>> portionMap = new HashMap<>();

        Map<Integer, String> ingredientNames = getIngredientNames(fdcIds);

        for (Integer fdcId : fdcIds) {
            String ingredientName = ingredientNames.get(fdcId);
            List<PortionModel> portionsList = getPortionsByFdcId(fdcId);
            portionMap.put(ingredientName, portionsList);
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