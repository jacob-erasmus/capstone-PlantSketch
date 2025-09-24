package plantsketch;

public class AbioticFactors {
    public MoistureMap moistureMap;
    public TemperatureMap temperatureMap;
    public SunlightMap sunlightMap;

    public AbioticFactors() {
        this.moistureMap = null;
        this.temperatureMap = null;
        this.sunlightMap = null;
    }

    public AbioticFactors(MoistureMap moistureMap, TemperatureMap temperatureMap, SunlightMap sunlightMap) {
        this.moistureMap = moistureMap;
        this.temperatureMap = temperatureMap;
        this.sunlightMap = sunlightMap;
    }

    // i had to add in these three getters becaue we did not have them
    public MoistureMap getMoistureMap() {
        return moistureMap;
    }

    public TemperatureMap getTemperatureMap() {
        return temperatureMap;
    }

    public SunlightMap getSunlightMap() {
        return sunlightMap;
    }

}
