package plantsketch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PinkNoiseSampler {

    private final float width;
    private final float height;
    private final float minDistance; // min spacing between points (e.g. 2m)
    private final Random rng;

    public PinkNoiseSampler(float width, float height, float minDistance, long seed) {
        this.width = width;
        this.height = height;
        this.minDistance = minDistance;
        this.rng = new Random();
// I (WILLIAM) MADE THE SEED RANDOM WAS : new Random(seed);
    }

    /**
     * Generate N point samples using dart throwing.
     *
     * @param n number of points to generate
     * @return list of PointSample
     */
    public List<PointSample> generateSamples(int n) {
        List<PointSample> samples = new ArrayList<>();

        int attempts = 0;
        while (samples.size() < n && attempts < n * 1000) {
            float x = rng.nextFloat() * width;
            float y = rng.nextFloat() * height;

            // can add in the checks for plants here and add the plant as you create the
            // point

            PointSample candidate = new PointSample(x, y);

            if (isFarEnough(candidate, samples)) {
                samples.add(candidate);
            }
            attempts++;
        }

        return samples;
    }

    private boolean isFarEnough(PointSample candidate, List<PointSample> samples) {
        for (PointSample s : samples) {
            float dx = candidate.getX() - s.getX();
            float dy = candidate.getY() - s.getY();
            float distSq = dx * dx + dy * dy;
            if (distSq < minDistance * minDistance) {
                return false; // too close
            }
        }
        return true;
    }
}
