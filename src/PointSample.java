public class PointSample {
    private float x;
    private float y;
    private Plant plant;

    public PointSample() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.plant = null;
    }

    public PointSample(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointSample(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
