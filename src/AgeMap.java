public class AgeMap {
    
    private int dimX;
    private int dimY;
    private float gridSpacing;
    private float[][] grid;
    
    public AgeMap() {
        this.dimX = 0;
        this.dimY = 0;
        this.gridSpacing = 0;
        this.grid = null;
    }
    
    public AgeMap(int width, int height, float gridSpacing, float[][] grid) {
        this.dimX = width;
        this.dimY = height;
        this.gridSpacing = gridSpacing;
        this.grid = grid;
    }
    
    public float getAge(int x, int y) {
        return grid[x][y];
    }

    public void testAge() {
        for (int i = 0; i < 13; i++) {
            System.out.println("Age at (0," + i + "): " + getAge(0,i));
        }
    }
    
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

