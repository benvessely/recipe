package benv.recipe.model;

public class PortionModel {
    private Integer portionId;
    private Integer fdcId;
    private Double unitAmount;
    private String unit;
    private Double weightGrams;

    // Getters and setters
    public Integer getPortionId() {
        return portionId;
    }

    public void setPortionId(Integer portionId) {
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

    public Double getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(Double weightGrams) {
        this.weightGrams = weightGrams;
    }

    @Override
    public String toString() {
        return "PortionModel{" +
                "portionId=" + portionId +
                ", fdcId=" + fdcId +
                ", unitAmount=" + unitAmount +
                ", unit='" + unit + '\'' +
                ", weightGrams=" + weightGrams +
                '}';
    }
}