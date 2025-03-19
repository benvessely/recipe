package benv.recipe.model;

public class NutritionValuesModel {
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private Double fiber;
    private Double totalSugar;
    private Double satFat;
    private Double cholesterol;
    private Double sodium;

    public Double getCalories() {
        return calories;
    }

    public Double getProtein() {
        return protein;
    }

    public Double getFat() {
        return fat;
    }

    public Double getCarbs() {
        return carbs;
    }

    public Double getFiber() {
        return fiber;
    }

    public Double getTotalSugar() {
        return totalSugar;
    }

    public Double getSatFat() {
        return satFat;
    }

    public Double getCholesterol() {
        return cholesterol;
    }

    public Double getSodium() {
        return sodium;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }

    public void setTotalSugar(Double totalSugar) {
        this.totalSugar = totalSugar;
    }

    public void setSatFat(Double satFat) {
        this.satFat = satFat;
    }

    public void setCholesterol(Double cholesterol) {
        this.cholesterol = cholesterol;
    }

    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }
}