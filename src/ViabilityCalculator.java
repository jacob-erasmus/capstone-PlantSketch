



public class ViabilityCalculator {
    
    private Terrain terrain;
    private AbioticFactors abioticFactors;
    private SunlightMap sunlightMap;
    private TemperatureMap temperatureMap;
    private MoistureMap moistureMap;
    double a = 0.2;
    
    public ViabilityCalculator()
    {
        this.terrain = null;
        this.abioticFactors = null;
    }
    
    public ViabilityCalculator(Terrain terrain, AbioticFactors abioticFactors)
    {
        // now we have the calculator loaded with all the info about everything
        // so for each random pink noise point we simply call one of the methods and return the viabilities of each species
        this.terrain = terrain;
        this.abioticFactors = abioticFactors;
        this.temperatureMap = abioticFactors.getTemperatureMap();
        this.sunlightMap = abioticFactors.getSunlightMap();
        this.moistureMap = abioticFactors.getMoistureMap();
    }
    
    public double calculation(double r, double c, double value)
    {
        double dx = Math.abs(value - c);
        double f = (1+a)*(Math.pow(Math.E,Math.pow(dx/r,4.5)*(Math.log(0.2))))-a; 
        // here is the calculation in all its glory. it is the same calc for all abiotic factors. 
        // r and c come from the plants files, and value is the abiotic value from the terrain

        return f;
    }



    public double viabililty(Species species, int x, int y)
    {
        // calculating the viablility wrt slope
        double cs = species.getSlopeC();
        double rs = species.getSlopeR();
        float slope = terrain.getSlope(x,y);
        double fs = calculation(rs, cs, slope);
        
        // calculating the viablility wrt temperature
        double ct = species.getTemperatureC();
        double rt = species.getTemperatureR();
        float temp = temperatureMap.getTemperature(x,y);
        double ft = calculation(rt, ct, temp);

        // calculating the viablility wrt sunlight
        double ce = species.getSunlightC();
        double re = species.getSunlightR();
        float sunl = sunlightMap.getSunlight(x,y);
        double fe = calculation(re, ce, sunl);

        // calculating the viablility wrt moisture
        double cm = species.getMoistureC();
        double rm = species.getMoistureR();
        float moist = moistureMap.getMoisture(x,y);
        double fm = calculation(rm, cm, moist);

        // now we take the minimum as that is the deciding factor on the viability of the species
        return Math.min(Math.min(fs,ft),Math.min(fe,fm));
    }
}