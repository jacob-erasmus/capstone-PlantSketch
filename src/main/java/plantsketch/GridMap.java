package plantsketch;

public class GridMap {
    protected int dimX;
    protected int dimY;
    protected float gridSpacing;
    protected float[][] grid;

    // Default constructor
    public GridMap() {
        this.dimX = 0;
        this.dimY = 0;
        this.gridSpacing = 0;
        this.grid = null;
    }

    // Parameterized constructor
    public GridMap(int width, int height, float gridSpacing, float[][] grid) {
        this.dimX = width;
        this.dimY = height;
        this.gridSpacing = gridSpacing;
        this.grid = grid;
    }

    // Generic getter
    public float getValue(int x, int y) {
        return grid[x][y];
    }

    // Generic tester
    public void testGrid(String name) {
        for (int i = 0; i < 13; i++) {
            System.out.println(name + " at (0," + i + "): " + getValue(0, i));
        }
    }

    // Grid modification methods
    public void setBase(int x, int y, float newValue) {
        grid[x][y] = newValue;
    }

    public void setAdjustment(int x, int y, float delta) {
        grid[x][y] += delta;
    }

    public void resetAdjustment(int x, int y, float originalValue) {
        grid[x][y] = originalValue;
    }
}
