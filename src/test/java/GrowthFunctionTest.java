
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import plantsketch.*;

public class GrowthFunctionTest {

    @Test
    void heightGrowsWithAge_OpenAllometry() {
        // Arrange: build a simple species + params
        GrowthParameters gp = new GrowthParameters(10f, 8f, -4f, 100f); // maxHeightOpen=10, q=4, lifeSpan=100
        ViabilityParameters vp = new ViabilityParameters(0, 0, 0, 0, 0, 0, 0, 0); // not used here
        Species sp = new Species("TestTree", vp, gp, "Green", 0.5f, 0.4f, 0.5f, 1.0f, "L", 1.0f);

        GrowthFunction gf = new GrowthFunction();

        // Act:
        float h10 = gf.calculateSize(sp, 10f, true);
        float h20 = gf.calculateSize(sp, 20f, true);
        float h50 = gf.calculateSize(sp, 50f, true);

        // Assert: monotonic increase and capped by maxHeightOpen
        assertTrue(h10 < h20 && h20 < h50, "Height should increase with age");
        assertTrue(h50 <= gp.getMaxHeightOpen() + 1e-3, "Should not exceed max height (open)");
    }

    @Test
    void respectsClosedAllometryMax() {
        GrowthParameters gp = new GrowthParameters(12f, 6f, 4f, 80f);
        ViabilityParameters vp = new ViabilityParameters(0, 0, 0, 0, 0, 0, 0, 0);
        Species sp = new Species("TestTree", vp, gp, "Green", 0.5f, 0.4f, 0.5f, 1.0f, "L", 1.0f);

        GrowthFunction gf = new GrowthFunction();

        float hLate = gf.calculateSize(sp, 100f, false); // age beyond lifespan
        assertTrue(hLate <= gp.getMaxHeightClosed() + 1e-3, "Closed canopy cap must apply");
    }
}
