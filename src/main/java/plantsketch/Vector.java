package plantsketch;

//

/**
 * extracts logic that the Terrain class uses to calculate the slope
 */
public class Vector {
    float x;
    float y;
    float z;

    // constructor
    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Normalise (want a unit vector)
    public void normalise() {
        double vectorLength = Math.sqrt(x * x + y * y + z * z);
        if (vectorLength > 0) {
            x /= vectorLength;
            y /= vectorLength;
            z /= vectorLength;
        }
    }

    // Find Dot Product
    public double dot(Vector b) {
        return this.x * b.x + this.y * b.y + this.z * b.z;
    }
}
