package benv.recipe.model;

public class IngredientSelectionModel {
    private Integer portionId;
    private Integer fdcId;
    private Double quantity;
    private String unit;

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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity (Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "IngredientSelectionModel{" +
                "portionId=" + portionId +
                ", fdcId=" + fdcId +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}

