public class Species {

    String name;
    float lifeSpan;
    float sunlightC;
    float sunlightR;
    float moistureC;
    float moistureR;
    float temperatureC;
    float temperatureR;
    float slopeC;
    float slopeR;
    String colour;

    float maxHeightOpen;
    float maxHeightClosed;
    float q;
    float radiusMultiplierOpen;
    float radiusMultiplierClosed;
    float leafTransparency;
    float moistureAbsorbtion;
    String growthPeriod;

    // Constructor
    public Species(String name, float lifeSpan, float sunlightC, float sunlightR,
                   float moistureC, float moistureR,
                   float temperatureC, float temperatureR,
                   float slopeC, float slopeR,
                   String colour,
                   float maxHeightOpen, float maxHeightClosed, float q,
                   float radiusMultiplierOpen, float radiusMultiplierClosed,
                   float leafTransparency, float moistureAbsorbtion, String growthPeriod ) {
        this.name = name;
        this.lifeSpan = lifeSpan;
        this.sunlightC = sunlightC;
        this.sunlightR = sunlightR;
        this.moistureC = moistureC;
        this.moistureR = moistureR;
        this.temperatureC = temperatureC;
        this.temperatureR = temperatureR;
        this.slopeC = slopeC;
        this.slopeR = slopeR;
        this.colour = colour;
        this.maxHeightOpen = maxHeightOpen;
        this.maxHeightClosed = maxHeightClosed;
        this.q = q;
        this.radiusMultiplierOpen = radiusMultiplierOpen;
        this.radiusMultiplierClosed = radiusMultiplierClosed;
        this.leafTransparency = leafTransparency;
        this.moistureAbsorbtion = moistureAbsorbtion;
        this.growthPeriod = growthPeriod;
    }
}
