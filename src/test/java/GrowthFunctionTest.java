
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plantsketch.*;

public class GrowthFunctionTest {

    Species species;
    GrowthFunction growthFunction;
    GrowthParameters growthParameters;
    ViabilityParameters viabilityParameters;

    @BeforeEach
    void setup(){
        growthParameters = new GrowthParameters(12f, 6f, 4f, 80f);
        viabilityParameters = new ViabilityParameters(0, 0, 0, 0, 0, 0, 0, 0);
        species = new Species("TestTree", "test", viabilityParameters, growthParameters, "Green", 0.5f, 0.4f, 0.5f, 1.0f, "L", 1.0f);
        growthFunction = new GrowthFunction();
    }

    @Test
    void heightGrowsWithAge_OpenAllometry() {

        float heightAge10 = growthFunction.calculateSize(species, 10f, true);
        float heightAge20 = growthFunction.calculateSize(species, 20f, true);
        float heightAge50 = growthFunction.calculateSize(species, 50f, true);

        // Assert: increase and capped by maxHeightOpen
        assertTrue(heightAge10 < heightAge20 && heightAge20 < heightAge50, "Height should increase with age");
        assertTrue(heightAge50 <= growthParameters.getMaxHeightOpen() + 1e-3, "Should not exceed max height (open)");
    }

    @Test
    void respectsClosedAllometryMax() {
        float heightTooOld = growthFunction.calculateSize(species, 100f, false); // age beyond lifespan
        assertTrue(heightTooOld <= growthParameters.getMaxHeightClosed() + 1e-3, "Closed canopy cap must apply");
    }
}
