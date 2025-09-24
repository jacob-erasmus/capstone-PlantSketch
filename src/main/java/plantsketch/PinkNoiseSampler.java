package plantsketch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PinkNoiseSampler {

    private final float width;
    private final float height;
    private final float minDistance;
    private final Random rng;

    public PinkNoiseSampler(float width, float height, float minDistance, long seed) {
        if (width <= 0 || height <= 0 || minDistance <= 0) {
            throw new IllegalArgumentException("width/height/minDistance must be > 0");
        }
        this.width = width;
        this.height = height;
        this.minDistance = minDistance;
        // this.rng = new Random(seed);
        this.rng = new Random(); // the seed doesn't actually make sense in this context. useful for when we do speedup graphs tho
    }

    public List<PointSample> generateSamples(int n) {
        if (n <= 0) return new ArrayList<>();

        // 1) candidates
        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = rng.nextFloat() * width;
            ys[i] = rng.nextFloat() * height;
        }

        // 2) prune via sparse grid
        final float cellSize = (float) (minDistance / Math.sqrt(2.0));
        final float invCell  = 1.0f / cellSize;

        var grid = new java.util.HashMap<Long, java.util.ArrayList<Integer>>(n * 2);
        final float minDist2 = minDistance * minDistance;
        ArrayList<PointSample> kept = new ArrayList<>(n);

        int[] order = shuffledIndices(n, rng);
        for (int oi = 0; oi < n; oi++) {
            int i = order[oi];
            float x = xs[i], y = ys[i];

            int gx = (int) (x * invCell);
            int gy = (int) (y * invCell);

            boolean tooClose = false;

            for (int by = gy - 1; by <= gy + 1 && !tooClose; by++) {
                for (int bx = gx - 1; bx <= gx + 1; bx++) {
                    var bucket = grid.get(pack(bx, by));
                    if (bucket == null) continue;
                    for (int idx : bucket) {
                        float dx = xs[idx] - x;
                        float dy = ys[idx] - y;
                        if (dx * dx + dy * dy < minDist2) {
                            tooClose = true;
                            break;
                        }
                    }
                }
            }

            if (!tooClose) {
                kept.add(new PointSample(x, y));
                long key = pack(gx, gy);
                var list = grid.get(key);
                if (list == null) {
                    list = new ArrayList<>(4);
                    grid.put(key, list);
                }
                list.add(i);

                // early exit if we already have N
                if (kept.size() >= n) break;
            }
        }

        return kept;
    }

    private static long pack(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL); // OR avoids collisions
    }

    private static int[] shuffledIndices(int n, Random rng) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
        }
        return a;
    }
}
