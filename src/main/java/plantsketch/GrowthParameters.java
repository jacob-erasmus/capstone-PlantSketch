// Handles growth function parameters
package plantsketch;

public class GrowthParameters {
    private float maxHeightOpen;
    private float maxHeightClosed;
    private float q;
    private float lifeSpan;

    public GrowthParameters(float maxHeightOpen, float maxHeightClosed,
            float q, float lifeSpan) {
        this.maxHeightOpen = maxHeightOpen;
        this.maxHeightClosed = maxHeightClosed;
        this.q = q;
        this.lifeSpan = lifeSpan;
    }

    public GrowthParameters(GrowthParameters other) {
        this.maxHeightOpen = other.maxHeightOpen;
        this.maxHeightClosed = other.maxHeightClosed;
        this.q = other.q;
        this.lifeSpan = other.lifeSpan;
    }

    // Getters
    public float getMaxHeightOpen() {
        return maxHeightOpen;
    }

    public float getMaxHeightClosed() {
        return maxHeightClosed;
    }

    public float getQ() {
        return q;
    }

    public float getLifeSpan() {
        return lifeSpan;
    }

    @Override
    public String toString() {
        return "GrowthParameters{" +
                "maxHeightOpen=" + maxHeightOpen +
                ", maxHeightClosed=" + maxHeightClosed +
                ", q=" + q +
                ", lifeSpan=" + lifeSpan +
                '}';
    }

    // SETTERS
    public void setMaxHeightOpen(float maxHeightOpen) {
        this.maxHeightOpen = maxHeightOpen;
    }

    public void setMaxHeightClosed(float maxHeightClosed) {
        this.maxHeightClosed = maxHeightClosed;
    }

    public void setQ(float q) {
        this.q = q;
    }

    public void setLifeSpan(float lifeSpan) {
        this.lifeSpan = lifeSpan;
    }
}
