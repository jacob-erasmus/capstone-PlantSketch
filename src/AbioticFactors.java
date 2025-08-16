public class AbioticFactors {
    public MoistureMap moistureMap;
    public TemperatureMap temperatureMap;
    public SunlightMap sunlightMap;
    
    public AbioticFactors() {
        this.moistureMap = null;
        this.temperatureMap = null;
        this.sunlightMap = null;
    }
    
    public AbioticFactors(int width, int height) {
        this.moistureMap = new MoistureMap(width, height);
        this.temperatureMap = new TemperatureMap(width, height);
        this.sunlightMap = new SunlightMap(width, height);
    }
    
    public float getAdjustedMoisture(int x, int y) {
        // Method stub
        return 0.0f;
    }
    
    public float getAdjustedTemperature(int x, int y) {
        // Method stub
        return 0.0f;
    }
    
    public float getAdjustedSunlight(int x, int y) {
        // Method stub
        return 0.0f;
    }
}
