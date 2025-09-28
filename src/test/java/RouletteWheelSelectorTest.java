import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;
import plantsketch.*;

public class RouletteWheelSelectorTest {

    private ViabilityCalculator viabilityCalculator;
    private Terrain terrain;
    private AbioticFactors abioticFactors;
    private List<Species> speciesList;

    @BeforeEach
    void setUp() {
        // Create test environment (2x2 grid) similar to ViabilityCalculatorTest
        float[][] elevation = {{100.0f, 105.0f}, {102.0f, 108.0f}};
        float[][] moisture = {{27.5f, 30.0f}, {25.0f, 32.0f}};
        float[][] sunlight = {{3.75f, 4.0f}, {3.5f, 4.5f}};
        float[][] temperature = {{11.75f, 12.0f}, {11.5f, 12.5f}};

        MoistureMap moistureMap = new MoistureMap(2, 2, 25.0f, moisture);
        SunlightMap sunlightMap = new SunlightMap(2, 2, 25.0f, sunlight);
        TemperatureMap temperatureMap = new TemperatureMap(2, 2, 25.0f, temperature);

        abioticFactors = new AbioticFactors(moistureMap, temperatureMap, sunlightMap);
        terrain = new Terrain(2, 2, 25.0f, abioticFactors, elevation);
        viabilityCalculator = new ViabilityCalculator(terrain, abioticFactors);

        // Load species using SpeciesDictionary like SimulationEngine does
        SpeciesDictionary dict = new SpeciesDictionary();
        speciesList = List.of(
            dict.loadBoxwood(),
            dict.loadSilverFir(),
            dict.loadMountainPine()
        );
    }

    @Test
    void testRouletteWheelSelection_RealisticViabilityCalculation() {
        // Arrange: Build roulette wheel exactly like SimulationEngine.placementLoop()
        int xCell = 0, yCell = 0; // Test at position (0,0)

        ArrayList<Species> candidates = new ArrayList<>();
        ArrayList<Table> wheel = new ArrayList<>();
        float sumV = 0f;
        float density = 0f;

        // Calculate real viabilities for each species at this location
        for (Species sp : speciesList) {
            float v = viabilityCalculator.viabililty(sp, xCell, yCell);
            sp.setViabilityAtPoint(v);

            if (v > 0f) {
                candidates.add(sp);
                density = Math.max(density, v);
                sumV += v;
                wheel.add(new Table(sp, sumV)); // Cumulative viability like SimulationEngine
            }
        }

        // Act: Test multiple selections with the realistic setup
        Map<String, Integer> selectionCounts = new HashMap<>();
        for (Species sp : candidates) {
            selectionCounts.put(sp.getName(), 0);
        }

        assertFalse(candidates.isEmpty(), "Should have viable species at test location");
        assertTrue(sumV > 0, "Total viability should be positive");

        int totalSelections = 5000;
        for (int i = 0; i < totalSelections; i++) {
            RouletteWheelSelector selector = new RouletteWheelSelector(sumV);
            Species selected = selector.selectSpecies(wheel, wheel.size());

            assertNotNull(selected, "Selector should always return a species from viable candidates");
            assertTrue(candidates.contains(selected), "Selected species should be in viable candidates");

            selectionCounts.put(selected.getName(), selectionCounts.get(selected.getName()) + 1);
        }

        // Assert: Verify selections are proportional to calculated viabilities
        System.out.println("Realistic Viability Test Results at position (0,0):");
        System.out.println("Environment: moisture=27.5, sunlight=3.75, temperature=11.75");
        System.out.println();

        for (Species sp : candidates) {
            float viability = sp.getViabilityAtPoint();
            double expectedProportion = viability / sumV;
            double actualProportion = (double) selectionCounts.get(sp.getName()) / totalSelections;

            System.out.printf("%s: viability=%.3f, expected=%.1f%%, actual=%.1f%%\n",
                sp.getName(), viability, expectedProportion * 100, actualProportion * 100);

            // Allow 5% tolerance for random variation
            assertEquals(expectedProportion, actualProportion, 0.05,
                String.format("%s selection should be proportional to its calculated viability", sp.getName()));
        }

        // Verify total selections add up correctly
        int totalCounted = selectionCounts.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(totalSelections, totalCounted, "All selections should be counted");
    }

    @Test
    void testRouletteWheelSelection_EmptyViabilityTable() {
        // Arrange: Empty wheel (no viable species)
        ArrayList<Table> emptyWheel = new ArrayList<>();
        RouletteWheelSelector selector = new RouletteWheelSelector(0.0f);

        // Act & Assert: Should return null for empty wheel
        Species result = selector.selectSpecies(emptyWheel, 0);
        assertNull(result, "Empty viability table should return null");
    }

    @Test
    void testRouletteWheelSelection_SingleViableSpecies() {
        // Arrange: Create scenario where only one species is viable
        ArrayList<Table> singleWheel = new ArrayList<>();
        Species boxwood = speciesList.get(0); // Boxwood

        float viability = viabilityCalculator.viabililty(boxwood, 0, 0);
        assertTrue(viability > 0, "Boxwood should be viable at test location");

        singleWheel.add(new Table(boxwood, viability));

        // Act: Test multiple selections
        for (int i = 0; i < 50; i++) {
            RouletteWheelSelector selector = new RouletteWheelSelector(viability);
            Species result = selector.selectSpecies(singleWheel, 1);

            // Assert: Should always return the single viable species
            assertNotNull(result, "Should return the single viable species");
            assertEquals("Boxwood", result.getName(), "Should always select the only viable species");
        }
    }
}