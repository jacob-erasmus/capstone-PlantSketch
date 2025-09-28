package plantsketch;

import plantsketch.util.PerformanceTimer;

// The terrain class stores all the of abiotic factors plus the elevation profile and is used extensively for referencing our 2d arrays

public class Terrain {
    int width;
    int height;
    float gridSpacing;
    float[][] elevationMap;
    float[][] slopeMap;
    AbioticFactors abioticFactors;
    TemperatureMap temperatureMap;
    MoistureMap moistureMap;
    SunlightMap sunlightMap;

    // Constructor
    public Terrain(int width, int height, float gridSpacing, AbioticFactors abioticFactors, float[][] elevation) {
        this.width = width;
        this.height = height;
        this.gridSpacing = gridSpacing;
        this.elevationMap = elevation;
        this.slopeMap = new float[width][height];
        this.abioticFactors = abioticFactors;

        // Pre-calculate all slope values once in order to save computation later in the program
        calculateSlopeGrid();
    }

    // Get Elevation Method
    public float getElevation(int x, int y) {
        return elevationMap[x][y];
    }

    // Get Slope Method - now returns cached value
    public float getSlope(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return slopeMap[x][y];
    }

    // Calculate slope for all grid positions and cache the results
    private void calculateSlopeGrid() {
        PerformanceTimer.start("slope_calculation_grid");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                slopeMap[x][y] = calculateSlopeAt(x, y);
            }
        }
        PerformanceTimer.end("slope_calculation_grid");
    }

    // Calculate slope at a specific position (extracted from original getSlope method)
    private float calculateSlopeAt(int x, int y) {
        // Boundary checking - use forward/backward differences for edges
        int maxX = width - 1;
        int maxY = height - 1;

        // Approximate partial derivatives using appropriate differences
        float dzdx, dzdy;

        // Handle x-direction derivative
        if (x == 0) {
            // Forward difference at left boundary
            dzdx = (elevationMap[x + 1][y] - elevationMap[x][y]) / gridSpacing;
        } else if (x == maxX) {
            // Backward difference at right boundary
            dzdx = (elevationMap[x][y] - elevationMap[x - 1][y]) / gridSpacing;
        } else {
            // Central difference for interior points
            dzdx = (elevationMap[x + 1][y] - elevationMap[x - 1][y]) / (2 * gridSpacing);
        }

        // Handle y-direction derivative
        if (y == 0) {
            // Forward difference at bottom boundary
            dzdy = (elevationMap[x][y + 1] - elevationMap[x][y]) / gridSpacing;
        } else if (y == maxY) {
            // Backward difference at top boundary
            dzdy = (elevationMap[x][y] - elevationMap[x][y - 1]) / gridSpacing;
        } else {
            // Central difference for interior points
            dzdy = (elevationMap[x][y + 1] - elevationMap[x][y - 1]) / (2 * gridSpacing);
        }

        // normal vector
        Vector n = new Vector(-dzdx, -dzdy, 1);
        n.normalise();
        // vertical vector
        Vector vertical = new Vector(0, 0, 1);
        double dotProduct = n.dot(vertical);
        return (float) Math.toDegrees(Math.acos(dotProduct));
    }

    // Set Abiotic Factors
    public void setAbioticFactors() {
        temperatureMap = abioticFactors.getTemperatureMap();
        sunlightMap = abioticFactors.getSunlightMap();
        moistureMap = abioticFactors.getMoistureMap();
    }

    public void testTerrain() {
        for (int i = 0; i < 13; i++) {
            System.out.println("Terrain at (0, " + i + "): " + getElevation(0, i));
        }
    }

    public float[][] getElevationGrid() {
        return elevationMap;
    }   

    public float[][] getSlopeGrid() {
        return slopeMap;
    }
}