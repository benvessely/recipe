package benv.recipe.model;

public class IngredientModel{
    private final String originalText;    // Original full text
    private Double amount;          // Numeric amount
    private String unit;           // Unit of measurement
    private String mainIngredient;     // Ingredient name/description

    public IngredientModel(String originalText) {
        this.originalText = originalText;
    }

    public String getOriginalText() {
        return originalText;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getMainIngredient() {
        return mainIngredient;
    }

    public void setMainIngredient(String mainIngredient) {
        this.mainIngredient = mainIngredient;
    }

    @Override
    public String toString() {
        return String.format("ParsedIngredient{amount=%s, unit='%s', mainIngredient='%s'}",
                amount, unit, mainIngredient);
    }
}