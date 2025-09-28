package plantsketch;

import plantsketch.util.PerformanceTimer;

/**
 * Calculates plant viability based on environmental factors using ecological modeling.
 * Evaluates how well a species can survive at a given location by considering
 * slope, temperature, sunlight, and moisture conditions.
 */
public class ViabilityCalculator {

    private Terrain terrain;
    private AbioticFactors abioticFactors;
    private SunlightMap sunlightMap;
    private TemperatureMap temperatureMap;
    private MoistureMap moistureMap;

    /** Maximal stress value constant used in viability calculations */
    double a = 0.2;

    /**
     * Creates an empty ViabilityCalculator with no terrain or environmental data.
     */
    public ViabilityCalculator() {
        this.terrain = null;
        this.abioticFactors = null;
    }

    /**
     * Creates a ViabilityCalculator with terrain and environmental data.
     * Initializes all environmental maps from the provided abiotic factors.
     *
     * @param terrain the terrain data containing slope information
     * @param abioticFactors environmental factors containing temperature, sunlight, and moisture maps
     */
    public ViabilityCalculator(Terrain terrain, AbioticFactors abioticFactors) {
        // Calculator is now loaded with all environmental information
        // For each sampling point, we can calculate species viabilities
        this.terrain = terrain;
        this.abioticFactors = abioticFactors;
        this.temperatureMap = abioticFactors.getTemperatureMap();
        this.sunlightMap = abioticFactors.getSunlightMap();
        this.moistureMap = abioticFactors.getMoistureMap();
    }

    /**
     * Calculates viability for a single environmental factor using an exponential stress function.
     * This is the core viability calculation applied uniformly across all abiotic factors.
     * Uses the formula: f = (1 + a) * e^((|value - c| / r)^4.5 * ln(0.2)) - a
     *
     * @param r species tolerance range parameter (from species data)
     * @param c species optimal value parameter (from species data)
     * @param value actual environmental value at the location
     * @return viability score for this environmental factor (0.0 to 1.0)
     */
    public double calculateViability(double r, double c, double value) {
        double dx = Math.abs(value - c); // Distance from optimal value
        // Exponential stress function with 4.5 power for steep decline outside tolerance
        double f = (1 + a) * (Math.pow(Math.E, Math.pow(dx / r, 4.5) * (Math.log(0.2)))) - a;

        return f;
    }

    /**
     * Calculates overall species viability at a specific location by evaluating
     * all environmental factors and returning the minimum (limiting factor).
     *
     * @param species the species to evaluate
     * @param x x-coordinate of the location
     * @param y y-coordinate of the location
     * @return overall viability score (0.0 to 1.0) limited by the most restrictive factor
     */
    public float viabililty(Species species, int x, int y) {
        PerformanceTimer.start("viability_calculation");

        // Calculate viability with respect to slope gradient
        double cs = species.getSlopeC();
        double rs = species.getSlopeR();
        float slope = terrain.getSlope(x, y);
        double fs = calculateViability(rs, cs, slope);

        // Calculate viability with respect to temperature conditions
        double ct = species.getTemperatureC();
        double rt = species.getTemperatureR();
        float temp = temperatureMap.getTemperature(x, y);
        double ft = calculateViability(rt, ct, temp);

        // Calculate viability with respect to sunlight exposure
        double ce = species.getSunlightC();
        double re = species.getSunlightR();
        float sunl = sunlightMap.getSunlight(x, y);
        double fe = calculateViability(re, ce, sunl);

        // Calculate viability with respect to moisture availability
        double cm = species.getMoistureC();
        double rm = species.getMoistureR();
        float moist = moistureMap.getMoisture(x, y);
        double fm = calculateViability(rm, cm, moist);

        // Apply limiting factor principle: the minimum viability determines overall survival
        // This reflects ecological reality where the most restrictive condition controls species presence
        float result = (float) Math.min(Math.min(fs, ft), Math.min(fe, fm));

        PerformanceTimer.end("viability_calculation");
        return result;
    }
}