public class Species {
    private String name;
    private double growthRate;
    private double sunlightPreference;
    private double moisturePreference;
    private double temperaturePreference;
    private double maxHeight;
    private double spreadRadius;
    
    public Species() {
        this.name = "";
        this.growthRate = 0.0;
        this.sunlightPreference = 0.0;
        this.moisturePreference = 0.0;
        this.temperaturePreference = 0.0;
        this.maxHeight = 0.0;
        this.spreadRadius = 0.0;
    }
    
    public Species(String name, double growthRate, double sunlightPreference, 
                   double moisturePreference, double temperaturePreference, 
                   double maxHeight, double spreadRadius) {
        this.name = name;
        this.growthRate = growthRate;
        this.sunlightPreference = sunlightPreference;
        this.moisturePreference = moisturePreference;
        this.temperaturePreference = temperaturePreference;
        this.maxHeight = maxHeight;
        this.spreadRadius = spreadRadius;
    }
    
    public double getViabilityAt(int x, int y, Terrain terrain) {
        // Method stub
        return 0.0;
    }
}
