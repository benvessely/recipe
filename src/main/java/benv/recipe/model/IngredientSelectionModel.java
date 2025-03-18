package benv.recipe.model;

public class IngredientSelectionModel {
    private Integer portionId;
    private Integer fdcId;
    private Double quantity;

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
}
