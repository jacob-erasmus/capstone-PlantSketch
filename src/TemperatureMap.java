public class TemperatureMap {
    private float[][] baseTMap;
    private float[][] adjustedTMap;
    
    public TemperatureMap() {
        this.baseTMap = null;
        this.adjustedTMap = null;
    }
    
    public TemperatureMap(int width, int height) {
        this.baseTMap = new float[height][width];
        this.adjustedTMap = new float[height][width];
    }
    
    public float getTemperature(int x, int y) {
        // Method stub
        return 0.0f;
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
