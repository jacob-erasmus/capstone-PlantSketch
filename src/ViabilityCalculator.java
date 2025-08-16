public class ViabilityCalculator {
    private Terrain terrain;
    private AbioticFactors abioticFactors;
    
    public ViabilityCalculator() {
        this.terrain = null;
        this.abioticFactors = null;
    }
    
    public ViabilityCalculator(Terrain terrain, AbioticFactors abioticFactors) {
        this.terrain = terrain;
        this.abioticFactors = abioticFactors;
    }
    
    public float calculateViability(Species species, float x, float y) {
        // Method stub
        return 0.0f;
    }
}