public class ViabilityCalculator {
    private double[] calculationParameters;
    
    public ViabilityCalculator() {
        this.calculationParameters = new double[0];
    }
    
    public ViabilityCalculator(double[] parameters) {
        this.calculationParameters = parameters.clone();
    }
    
    public double calculateViability(Species species, Terrain terrain, int x, int y) {
        // Method stub
        return 0.0;
    }
    
    public double getEnvironmentalSuitability(Species species, AbioticFactors abioticFactors, int x, int y) {
        // Method stub
        return 0.0;
    }
}