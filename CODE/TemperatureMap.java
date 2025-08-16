public class TemperatureMap {
    private double[][] data;
    private int width;
    private int height;
    
    public TemperatureMap() {
        this.width = 0;
        this.height = 0;
        this.data = null;
    }
    
    public TemperatureMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new double[height][width];
    }
    
    public double getTemperatureAt(int x, int y) {
        // Method stub
        return 0.0;
    }
}
