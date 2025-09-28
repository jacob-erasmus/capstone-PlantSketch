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