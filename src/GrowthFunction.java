public class GrowthFunction {
    

    public GrowthFunction() {  
        
    }
    
    public float calculateSize(Plant plant) {

        float q = plant.getQ();
        float lifeSpan = plant.getLifeSpan();
        float currentAge = plant.getAge();
        float maxHeight;

        //check the allometry of the plant at that point
        if (plant.isAllometryOpen()){
            maxHeight = plant.getMaxHeightOpen();
        }else{
            maxHeight = plant.getMaxHeightClosed(); 
        }

        float plantHeight = (2 / (1 + (Math.exp((currentAge/lifeSpan)*q))) - 1) * maxHeight;
        // where t is the current tree currentAge in years, tm is the maximum achievable tree currentAge in years,
        // mh is maximum achieve height in metres.
        return plantHeight;
    }
}