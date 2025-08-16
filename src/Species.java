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
    Colour colour;  // Assuming Colour is a class you've defined or imported

    // Constructor
    public Species(String name, float lifeSpan, float minSunlight, float maxSunlight,
                 float minMoisture, float maxMoisture,
                 float minTemperature, float maxTemperature,
                 float minSlope, float maxSlope,
                 Colour colour) {
        this.name = name;
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
