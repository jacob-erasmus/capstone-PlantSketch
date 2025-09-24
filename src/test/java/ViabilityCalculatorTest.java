import org.junit.jupiter.api.Test;
import plantsketch.GrowthParameters;
import plantsketch.Species;
import plantsketch.ViabilityParameters;

public class ViabilityCalculatorTest {

    //setting up

    GrowthParameters growthParameters = new GrowthParameters(10f, 8f, -4f, 100f); // maxHeightOpen=10, q=4, lifeSpan=100
    ViabilityParameters viabilityParameters = new ViabilityParameters(6, 5.50f, 25.00f, 25.00f, 0, 15, 0, 0); // not used here
    Species sp = new Species("TestTree", "test", viabilityParameters, growthParameters, "Green", 0.5f, 0.4f, 0.5f, 1.0f, "L", 1.0f);


    @Test
    void calculatesValueAsExpected(){
        assertEquals
    }
}
