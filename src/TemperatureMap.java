public class TemperatureMap {
    
    private int dimX;
    private int dimY;
    private float gridSpacing;
    private float[][] grid;
    
    public TemperatureMap() 
    {
        this.dimX = 0;
        this.dimY = 0;
        this.gridSpacing = 0;
        this.grid = null;
    }
    
    public TemperatureMap(int width, int height, float gridSpacing, float[][] grid) 
    {
        this.dimX = width;
        this.dimY = height;
        this.gridSpacing = gridSpacing;
        this.grid = grid;
    }
    
    public float getTemperature(int x, int y) {
        
        return grid[x][y];
    }

    public void testTemperature()
    {
        for(int i = 0; i < 13; i++) {
            System.out.println("Temperature at (0, "+i+"): " + getTemperature(0, i));
        }
    }
    
    public void setBase(int x, int y, float newValue) {
        // Method stub
    }
    
    public void setAdjustment(int x, int y, float newValue) {
        // Method stub
    }
    
    public void resetAdjustment(int x, int y, float newValue) {
        // Method stub
    }
}
