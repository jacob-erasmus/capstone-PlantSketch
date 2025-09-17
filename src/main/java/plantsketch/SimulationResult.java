package plantsketch;

import java.util.List;

/** Immutable DTO returned by SimulationRunner for the UI to render. */
public record SimulationResult(
        Forest forest,
        List<PointSample> samples,
        int dimX,
        int dimY,
        float gridSpacing,
        float[][] elevationGrid
) {}
