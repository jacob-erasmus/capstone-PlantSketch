public class SunlightMap {
    private double[][] data;
    private int width;
    private int height;
    
    public SunlightMap() {
        this.width = 0;
        this.height = 0;
        this.data = null;
    }
    
    public SunlightMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new double[height][width];
    }
    
    public double getSunlightAt(int x, int y) {
        // Method stub
        return 0.0;
    }
}