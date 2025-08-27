import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;

public class PinkNoiseSamplerTest {

    @Test
    void deterministicWithSeed() {
        PinkNoiseSampler s1 = new PinkNoiseSampler(100f, 100f, 2.0f, 42L);
        PinkNoiseSampler s2 = new PinkNoiseSampler(100f, 100f, 2.0f, 42L);

        List<PointSample> a = s1.generateSamples(200);
        List<PointSample> b = s2.generateSamples(200);

        assertEquals(a.size(), b.size(), "Same seed = same count");
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getX(), b.get(i).getX(), 1e-6);
            assertEquals(a.get(i).getY(), b.get(i).getY(), 1e-6);
        }
    }

    @Test
    void enforcesMinSeparation() {
        PinkNoiseSampler s = new PinkNoiseSampler(50f, 50f, 2.0f, 7L);
        List<PointSample> pts = s.generateSamples(150);

        float minD2 = (2.0f * 2.0f); // squared distance to avoid sqrt
        for (int i = 0; i < pts.size(); i++) {
            for (int j = i + 1; j < pts.size(); j++) {
                float dx = pts.get(i).getX() - pts.get(j).getX();
                float dy = pts.get(i).getY() - pts.get(j).getY();
                float d2 = dx * dx + dy * dy;
                assertTrue(d2 >= minD2 - 1e-6, "No two samples should be closer than 2m");
            }
        }
    }
}
