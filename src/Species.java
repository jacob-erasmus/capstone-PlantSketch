public class Species {

    // Basic info
    private String name;

    // Encapsulated parameter objects
    private ViabilityParameters viabilityParameters;
    private GrowthParameters growthParameters;

    // Other traits
    private float radiusMultiplierOpen;
    private float radiusMultiplierClosed;
    private float leafTransparency;
    private float moistureAbsorbtion;
    private String growthPeriod;
    private String colour;

    // Constructor
    public Species(String name,
                   ViabilityParameters viabilityParameters,
                   GrowthParameters growthParameters,
                   String colour,
                   float radiusMultiplierOpen,
                   float radiusMultiplierClosed,
                   float leafTransparency,
                   float moistureAbsorbtion,
                   String growthPeriod) {

        this.name = name;
        this.viabilityParameters = viabilityParameters;
        this.growthParameters = growthParameters;
        this.colour = colour;
        this.radiusMultiplierOpen = radiusMultiplierOpen;
        this.radiusMultiplierClosed = radiusMultiplierClosed;
        this.leafTransparency = leafTransparency;
        this.moistureAbsorbtion = moistureAbsorbtion;
        this.growthPeriod = growthPeriod;
    }

    // ===== Core getters =====
    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }

    public float getRadiusMultiplierOpen() {
        return radiusMultiplierOpen;
    }

    public float getRadiusMultiplierClosed() {
        return radiusMultiplierClosed;
    }

    public float getLeafTransparency() {
        return leafTransparency;
    }

    public float getMoistureAbsorbtion() {
        return moistureAbsorbtion;
    }

    public String getGrowthPeriod() {
        return growthPeriod;
    }

    //Viability getters
    public float getSunlightC() {
        return viabilityParameters.getSunlightC();
    }

    public float getSunlightR() {
        return viabilityParameters.getSunlightR();
    }

    public float getMoistureC() {
        return viabilityParameters.getMoistureC();
    }

    public float getMoistureR() {
        return viabilityParameters.getMoistureR();
    }

    public float getTemperatureC() {
        return viabilityParameters.getTemperatureC();
    }

    public float getTemperatureR() {
        return viabilityParameters.getTemperatureR();
    }

    public float getSlopeC() {
        return viabilityParameters.getSlopeC();
    }

    public float getSlopeR() {
        return viabilityParameters.getSlopeR();
    }

    //Growth getters
    public float getMaxHeightOpen() {
        return growthParameters.getMaxHeightOpen();
    }

    public float getMaxHeightClosed() {
        return growthParameters.getMaxHeightClosed();
    }

    public float getQ() {
        return growthParameters.getQ();
    }

    public float getLifeSpan() {
        return growthParameters.getLifeSpan();
    }

    @Override
    public String toString() {
        return "Species {" +
                "\n  name='" + name + '\'' +
                ",\n  viability=" + viabilityParameters +
                ",\n  growth=" + growthParameters +
                ",\n  colour='" + colour + '\'' +
                ",\n  radiusMultiplierOpen=" + radiusMultiplierOpen +
                ",\n  radiusMultiplierClosed=" + radiusMultiplierClosed +
                ",\n  leafTransparency=" + leafTransparency +
                ",\n  moistureAbsorbtion=" + moistureAbsorbtion +
                ",\n  growthPeriod='" + growthPeriod + '\'' +
                "\n}";
    }
}
