public class AgeMap extends GridMap {

    public AgeMap() { super(); }

    public AgeMap(int width, int height, float gridSpacing, float[][] grid) {
        super(width, height, gridSpacing, grid);
    }

    public float getAge(int x, int y) {
        return getValue(x, y);
    }

    public void testAge() {
        testGrid("Age");
    }
}

