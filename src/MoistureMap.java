public class MoistureMap {
    
    private int dimX;
    private int dimY;
    private float gridSpacing;
    private float[][] grid;
    
    public MoistureMap() 
    {
        this.dimX = 0;
        this.dimY = 0;
        this.gridSpacing = 0;
        this.grid = null;
    }
    
    public MoistureMap(int width, int height, float gridSpacing, float[][] grid) 
    {
        this.dimX = width;
        this.dimY = height;
        this.gridSpacing = gridSpacing;
        this.grid = grid;
    }
    
    public float getMoisture(int x, int y) {
        
        return grid[x][y];
    }

    public void testMoisture()
    {
        for(int i = 0; i < 13; i++) {
            System.out.println("Moisture at (0," + i + "): " + getMoisture(0,i));
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