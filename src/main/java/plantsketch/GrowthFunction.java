package plantsketch;

public class GrowthFunction {

    public GrowthFunction() {

    }

    public float calculateSize(Species species, float currentAge, boolean allometryIsOpen) {
        float q = species.getQ();
        float lifeSpan = species.getLifeSpan();
        float maxHeight;

        if (allometryIsOpen) {
            maxHeight = species.getMaxHeightOpen();
        } else {
            maxHeight = species.getMaxHeightClosed();
        }
        
        float plantHeight = (float) ((2.0 / (1.0 + Math.exp((currentAge / lifeSpan) * q))) - 1.0) * maxHeight;
        return plantHeight;
    }
}