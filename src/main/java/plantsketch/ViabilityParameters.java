// Handles tolerance ranges for abiotic conditions
package plantsketch;

public class ViabilityParameters {
    private float sunlightC;
    private float sunlightR;
    private float moistureC;
    private float moistureR;
    private float temperatureC;
    private float temperatureR;
    private float slopeC;
    private float slopeR;

    public ViabilityParameters(float sunlightC, float sunlightR,
            float moistureC, float moistureR,
            float temperatureC, float temperatureR,
            float slopeC, float slopeR) {
        this.sunlightC = sunlightC;
        this.sunlightR = sunlightR;
        this.moistureC = moistureC;
        this.moistureR = moistureR;
        this.temperatureC = temperatureC;
        this.temperatureR = temperatureR;
        this.slopeC = slopeC;
        this.slopeR = slopeR;
    }

    // Getters
    public float getSunlightC() {
        return sunlightC;
    }

    public float getSunlightR() {
        return sunlightR;
    }

    public float getMoistureC() {
        return moistureC;
    }

    public float getMoistureR() {
        return moistureR;
    }

    public float getTemperatureC() {
        return temperatureC;
    }

    public float getTemperatureR() {
        return temperatureR;
    }

    public float getSlopeC() {
        return slopeC;
    }

    public float getSlopeR() {
        return slopeR;
    }

    @Override
    public String toString() {
        return "ViabilityParameters{" +
                "sunlightC=" + sunlightC +
                ", sunlightR=" + sunlightR +
                ", moistureC=" + moistureC +
                ", moistureR=" + moistureR +
                ", temperatureC=" + temperatureC +
                ", temperatureR=" + temperatureR +
                ", slopeC=" + slopeC +
                ", slopeR=" + slopeR +
                '}';
    }

    // SETTERS

    public void setSunlightC(float sunlightC) {
        this.sunlightC = sunlightC;
    }

    public void setSunlightR(float sunlightR) {
        this.sunlightR = sunlightR;
    }

    public void setMoistureC(float moistureC) {
        this.moistureC = moistureC;
    }

    public void setMoistureR(float moistureR) {
        this.moistureR = moistureR;
    }

    public void setTemperatureC(float temperatureC) {
        this.temperatureC = temperatureC;
    }

    public void setTemperatureR(float temperatureR) {
        this.temperatureR = temperatureR;
    }

    public void setSlopeC(float slopeC) {
        this.slopeC = slopeC;
    }

    public void setSlopeR(float slopeR) {
        this.slopeR = slopeR;
    }
}
