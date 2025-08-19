public class GrowthFunction {
    

    public GrowthFunction() {  
        
    }
    
    public float calculateSize(Plant plant) {
        float q = plant.getQ();
        float lifeSpan = plant.getLifeSpan();
        float currentAge = plant.getAge();
        float maxHeight;

        if (plant.isAllometryOpen()) {
            maxHeight = plant.getMaxHeightOpen();
        } else {
            maxHeight = plant.getMaxHeightClosed();
        }

        float plantHeight = (float) ((2.0 / (1.0 + Math.exp((currentAge / lifeSpan) * q))) - 1.0) * maxHeight;
        return plantHeight;
    }
}