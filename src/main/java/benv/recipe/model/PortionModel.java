package benv.recipe.model;

public class PortionModel {
    private Integer portionId;
    private Integer fdcId;
    private Double unitAmount;
    private String unit;
    private Double weightPer100g;

    // Getters and setters
    public Integer getPortionId() {
        return portionId;
    }

    public void setId(Integer portionId) {
        this.portionId = portionId;
    }

    public Integer getFdcId() {
        return fdcId;
    }

    public void setFdcId(Integer fdcId) {
        this.fdcId = fdcId;
    }

    public Double getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(Double unitAmount) {
        this.unitAmount = unitAmount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getWeightPer100g() {
        return weightPer100g;
    }

    public void setWeightPer100g(Double weightPer100g) {
        this.weightPer100g = weightPer100g;
    }
}