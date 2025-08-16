public class Brush {
    private String brushType;
    private int size;
    private double intensity;
    
    public Brush() {
        this.brushType = "default";
        this.size = 1;
        this.intensity = 1.0;
    }
    
    public Brush(String brushType, int size, double intensity) {
        this.brushType = brushType;
        this.size = size;
        this.intensity = intensity;
    }
    
    public void apply(int x, int y, Object canvas) {
        // Method stub
    }
    
    public void setProperties(String brushType, int size, double intensity) {
        // Method stub
    }
}