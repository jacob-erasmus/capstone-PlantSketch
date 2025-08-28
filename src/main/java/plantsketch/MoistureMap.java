package plantsketch;

public class MoistureMap extends GridMap {

    public MoistureMap() {
        super();
    }

    public MoistureMap(int width, int height, float gridSpacing, float[][] grid) {
        super(width, height, gridSpacing, grid);
    }

    public float getMoisture(int x, int y) {
        return getValue(x, y);
    }

    public void testMoisture() {
        testGrid("Moisture");
    }
}
