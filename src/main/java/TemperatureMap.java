public class TemperatureMap extends GridMap {

    public TemperatureMap() { super(); }

    public TemperatureMap(int width, int height, float gridSpacing, float[][] grid) {
        super(width, height, gridSpacing, grid);
    }

    public float getTemperature(int x, int y) {
        return getValue(x, y);
    }

    public void testTemperature() {
        testGrid("Temperature");
    }
}
