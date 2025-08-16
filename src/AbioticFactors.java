public class AbioticFactors {
    
    private SunlightMap sunlightMap;
    private MoistureMap moistureMap;
    private TemperatureMap temperatureMap;
    
    public AbioticFactors() {
        this.sunlightMap = null;
        this.moistureMap = null;
        this.temperatureMap = null;
    }
    
    public AbioticFactors(SunlightMap sunlightMap, MoistureMap moistureMap, TemperatureMap temperatureMap) {
        this.sunlightMap = sunlightMap;
        this.moistureMap = moistureMap;
        this.temperatureMap = temperatureMap;
    }
    
    public double getSunlightAt(int x, int y) {
        // Method stub
        return 0.0;
    }
    
    public double getMoistureAt(int x, int y) {
        // Method stub
        return 0.0;
    }
    
    public double getTemperatureAt(int x, int y) {
        // Method stub
        return 0.0;
    }
}