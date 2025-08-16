public class Terrain {
    private int width;
    private int height;
    private double[][] elevationData;
    private AbioticFactors abioticFactors;
    
    public Terrain() {
        this.width = 0;
        this.height = 0;
        this.elevationData = null;
        this.abioticFactors = null;
    }
    
    public Terrain(int width, int height) {
        this.width = width;
        this.height = height;
        this.elevationData = new double[height][width];
        this.abioticFactors = new AbioticFactors();
    }
    
    public double getElevationAt(int x, int y) {
        // Method stub
        return 0.0;
    }
    
    public boolean isValidPosition(int x, int y) {
        // Method stub
        return false;
    }
}