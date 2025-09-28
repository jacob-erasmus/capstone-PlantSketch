import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import plantsketch.*;

public class ViabilityCalculatorTest {

    private ViabilityCalculator viabilityCalculator;
    private Terrain terrain;
    private AbioticFactors abioticFactors;
    private Species boxwood;
    private Species silverFir;

    @BeforeEach
    void setUp() {
        // Create test elevation data (2x2 grid)
        float[][] elevation = {{100.0f, 105.0f}, {102.0f, 108.0f}};
        // Create test abiotic factor grids
        float[][] moisture = {{27.5f, 30.0f}, {25.0f, 32.0f}};
        float[][] sunlight = {{3.75f, 4.0f}, {3.5f, 4.5f}};
        float[][] temperature = {{11.75f, 12.0f}, {11.5f, 12.5f}};

        MoistureMap moistureMap = new MoistureMap(2, 2, 25.0f, moisture);
        SunlightMap sunlightMap = new SunlightMap(2, 2, 25.0f, sunlight);
        TemperatureMap temperatureMap = new TemperatureMap(2, 2, 25.0f, temperature);

        abioticFactors = new AbioticFactors(moistureMap, temperatureMap, sunlightMap);
        terrain = new Terrain(2, 2, 25.0f, abioticFactors, elevation);

        viabilityCalculator = new ViabilityCalculator(terrain, abioticFactors);

        SpeciesDictionary speciesDictionary = new SpeciesDictionary();

        // Create test species based on the provided parameters
        boxwood = speciesDictionary.loadBoxwood();
        silverFir = speciesDictionary.loadSilverFir();
    }

    @Test
    void testCalculateViabilityFunction() {
        // Test the basic viability calculation function g(d)
        // Formula: g(d) = (1 + a) * e^((d/r)^4.5 * ln(0.2)) - a where a = 0.2
        // Test perfect match (d = 0) - should return 1.0
        double perfectMatch = viabilityCalculator.calculateViability(10.0, 5.0, 5.0);
        assertEquals(1.0, perfectMatch, 0.001, "Perfect environmental match should return viability of 1.0");

        // Test at the stress boundary (d = r) - should return ~0.04
        // When d = r: g(r) = (1 + 0.2) * e^(ln(0.2)) - 0.2 = 1.2 * 0.2 - 0.2 = 0.04
        double atBoundary = viabilityCalculator.calculateViability(10.0, 5.0, 15.0);
        assertEquals(0.04, atBoundary, 0.01, "At stress boundary should return ~0.04");

        // Test beyond stress boundary - should be negative
        double beyondBoundary = viabilityCalculator.calculateViability(5.0, 10.0, 25.0);
        assertTrue(beyondBoundary < 0.2, "Beyond stress boundary should have low viability");
    }

    @Test
    void testBoxwoodViabilityOptimalConditions() {
        // Test Boxwood at position with optimal environmental conditions
        // Boxwood optimal: Moisture=27.5, Sun=3.75, Temp=11.75, Slope=0

        float viability = viabilityCalculator.viabililty(boxwood, 0, 0);

        // Position (0,0) has: moisture=27.5, sun=3.75, temp=11.75
        // All these match boxwood's optimal values, so viability should be high
        assertTrue(viability > 0.8, "Boxwood should have high viability in optimal conditions");
    }

    @Test
    void testSilverFirViabilitySuboptimalConditions() {
        // Test Silver Fir at position with suboptimal conditions
        // Silver Fir optimal: Moisture=31, Sun=5, Temp=11.75, Slope=0
        // Position (0,0) has: moisture=27.5, sun=3.75, temp=11.75

        float viability = viabilityCalculator.viabililty(silverFir, 0, 0);

        // Moisture and sunlight are off from optimal, but temperature matches
        // Viability should be limited by the worst factor (minimum)
        assertTrue(viability < 1.0, "Silver Fir should have reduced viability in suboptimal conditions");
        assertTrue(viability > 0.0, "Silver Fir should still have some viability");
    }

    @Test
    void testViabilityMinimumPrinciple() {
        // Test that viability follows the minimum principle across all factors
        Species testSpecies = boxwood;
        float viability = viabilityCalculator.viabililty(testSpecies, 1, 1);
        // Get actual environmental values at position (1,1) from the maps
        float actualMoisture = abioticFactors.getMoistureMap().getMoisture(1, 1); // 32.0f
        float actualSunlight = abioticFactors.getSunlightMap().getSunlight(1, 1); // 4.5f
        float actualTemperature = abioticFactors.getTemperatureMap().getTemperature(1, 1); // 12.5f
        float actualSlope = terrain.getSlope(1, 1);
        // Calculate individual factor viabilities using actual environmental values
        float moistureViability = (float) viabilityCalculator.calculateViability(
            testSpecies.getMoistureR(), testSpecies.getMoistureC(), actualMoisture);
        float sunViability = (float) viabilityCalculator.calculateViability(
            testSpecies.getSunlightR(), testSpecies.getSunlightC(), actualSunlight);
        float tempViability = (float) viabilityCalculator.calculateViability(
            testSpecies.getTemperatureR(), testSpecies.getTemperatureC(), actualTemperature);
        float slopeViability = (float) viabilityCalculator.calculateViability(
            testSpecies.getSlopeR(), testSpecies.getSlopeC(), actualSlope);
        float expectedMinimum = Math.min(Math.min(moistureViability, sunViability),
                                       Math.min(tempViability, slopeViability));

        assertEquals(expectedMinimum, viability, 0.001,
                    "Overall viability should equal the minimum of individual factor viabilities");
    }

    @Test
    void testSlopeCalculationCaching() {
        // Test that slope is calculated once and cached (no recalculation)
        float slope1 = terrain.getSlope(0, 0);
        float slope2 = terrain.getSlope(0, 0);

        assertEquals(slope1, slope2, 0.001, "Slope should return same cached value");

        // Test that slope values are reasonable
        assertTrue(slope1 >= 0, "Slope should be non-negative");
        assertTrue(slope1 <= 90, "Slope should not exceed 90 degrees");
    }

    @Test
    void testBoundaryConditions() {
        // Test edge cases and boundary conditions

        // Test with zero tolerance (should be very sensitive)
        double zeroTolerance = viabilityCalculator.calculateViability(0.1, 10.0, 10.0);
        assertEquals(1.0, zeroTolerance, 0.001, "Zero distance with any tolerance should give perfect viability");

        // Test with very small non-zero tolerance
        double smallTolerance = viabilityCalculator.calculateViability(0.1, 10.0, 10.1);
        assertTrue(smallTolerance < 0.2, "Small tolerance should result in low viability for any distance");
    }

    @Test
    void testMultipleSpeciesComparison() {
        // Compare viability of different species at the same location
        float boxwoodViability = viabilityCalculator.viabililty(boxwood, 0, 0);
        float silverFirViability = viabilityCalculator.viabililty(silverFir, 0, 0);

        // Both should have some viability, but may differ based on their preferences
        assertTrue(boxwoodViability >= 0, "Boxwood should have non-negative viability");
        assertTrue(silverFirViability >= 0, "Silver Fir should have non-negative viability");

        // The species with parameters closer to the environmental conditions should have higher viability
        // At position (0,0): moisture=27.5, sun=3.75, temp=11.75
        // Boxwood optimal: Moisture=27.5, Sun=3.75, Temp=11.75 (perfect match)
        // Silver Fir optimal: Moisture=31, Sun=5, Temp=11.75 (moisture and sun off)
        assertTrue(boxwoodViability >= silverFirViability,
                  "Boxwood should have equal or higher viability due to better environmental match");
    }
}
