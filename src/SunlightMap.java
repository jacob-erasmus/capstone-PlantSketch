public class SunlightMap extends GridMap {

    public SunlightMap() { super(); }

    public SunlightMap(int width, int height, float gridSpacing, float[][] grid) {
        super(width, height, gridSpacing, grid);
    }

    public float getSunlight(int x, int y) {
        return getValue(x, y);
    }

    public void testSunlight() {
        testGrid("Sunlight");
    }
}
