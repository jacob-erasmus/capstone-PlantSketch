// Handles growth function parameters
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
}
