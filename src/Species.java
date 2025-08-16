public class Species {

    String name;
    float lifeSpan;
    float minSunlight;
    float maxSunlight;
    float minMoisture;
    float maxMoisture;
    float minTemperature;
    float maxTemperature;
    float minSlope;
    float maxSlope;
    String colour

    // Constructor
    public Species(String name, float lifeSpan, float minSunlight, float maxSunlight,
                 float minMoisture, float maxMoisture,
                 float minTemperature, float maxTemperature,
                 float minSlope, float maxSlope,
                 String colour) {
        this.name = name;
        this.lifeSpan = lifeSpan;
        this.minSunlight = minSunlight;
        this.maxSunlight = maxSunlight;
        this.minMoisture = minMoisture;
        this.maxMoisture = maxMoisture;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minSlope = minSlope;
        this.maxSlope = maxSlope;
        this.colour = colour;
    }
}
