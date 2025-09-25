package plantsketch;

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
    private float viabilityAtPoint;
    private String mnemonic; 

    // Constructor
    public Species(String name, 
            String mnemonic,
            ViabilityParameters viabilityParameters,
            GrowthParameters growthParameters,
            String colour,
            float radiusMultiplierOpen,
            float radiusMultiplierClosed,
            float leafTransparency,
            float moistureAbsorbtion,
            String growthPeriod,
            float viabilityAtPoint) {

        this.name = name;
        this.mnemonic = mnemonic;
        this.viabilityParameters = viabilityParameters;
        this.growthParameters = growthParameters;
        this.colour = colour;
        this.radiusMultiplierOpen = radiusMultiplierOpen;
        this.radiusMultiplierClosed = radiusMultiplierClosed;
        this.leafTransparency = leafTransparency;
        this.moistureAbsorbtion = moistureAbsorbtion;
        this.growthPeriod = growthPeriod;
        this.viabilityAtPoint = viabilityAtPoint;
    }

    // ===== Core getters =====
    public String getName() {
        return name;
    }

    public String getMnemonic() {
        return mnemonic;
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

    // Viability getters
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

    // Growth getters
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

    // point set & get
    public void setViabilityAtPoint(double viabililty) {
        viabilityAtPoint = (float) viabililty;
    }

    public float getViabilityAtPoint() {
        return viabilityAtPoint;
    }

    @Override
    public String toString() {
        return "Species {" +
                "\n  name='" + name + '\'' +
                ",\n  mnemonic='" + mnemonic + '\'' +
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

    // SETTERS

    public void setName(String name) {
        this.name = name;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setRadiusMultiplierOpen(float radiusMultiplierOpen) {
        this.radiusMultiplierOpen = radiusMultiplierOpen;
    }

    public void setRadiusMultiplierClosed(float radiusMultiplierClosed) {
        this.radiusMultiplierClosed = radiusMultiplierClosed;
    }

    public void setLeafTransparency(float leafTransparency) {
        this.leafTransparency = leafTransparency;
    }

    public void setMoistureAbsorbtion(float moistureAbsorbtion) {
        this.moistureAbsorbtion = moistureAbsorbtion;
    }

    public void setGrowthPeriod(String growthPeriod) {
        this.growthPeriod = growthPeriod;
    }

    // Viability setters
    public void setSunlightC(float sunlightC) {
        viabilityParameters.setSunlightC(sunlightC);
    }

    public void setSunlightR(float sunlightR) {
        viabilityParameters.setSunlightR(sunlightR);
    }

    public void setMoistureC(float moistureC) {
        viabilityParameters.setMoistureC(moistureC);
    }

    public void setMoistureR(float moistureR) {
        viabilityParameters.setMoistureR(moistureR);
    }

    public void setTemperatureC(float temperatureC) {
        viabilityParameters.setTemperatureC(temperatureC);
    }

    public void setTemperatureR(float temperatureR) {
        viabilityParameters.setTemperatureR(temperatureR);
    }

    public void setSlopeC(float slopeC) {
        viabilityParameters.setSlopeC(slopeC);
    }

    public void setSlopeR(float slopeR) {
        viabilityParameters.setSlopeR(slopeR);
    }

    // Growth setters
    public void setMaxHeightOpen(float maxHeightOpen) {
        growthParameters.setMaxHeightOpen(maxHeightOpen);
    }

    public void setMaxHeightClosed(float maxHeightClosed) {
        growthParameters.setMaxHeightClosed(maxHeightClosed);
    }

    public void setQ(float q) {
        growthParameters.setQ(q);
    }

    public void setLifeSpan(float lifeSpan) {
        growthParameters.setLifeSpan(lifeSpan);
    }
}
