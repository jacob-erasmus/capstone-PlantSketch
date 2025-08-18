public class GrowthFunction {
    

    public GrowthFunction() {  
        
    }
    
    public float calculateSize(Plant plant) {

        float q = plant.getQ();
        float lifeSpan = plant.getLifeSpan();
        float age = plant.getAge();

        if (plant.isAllometryOpen()){
            float maxHeight = plant.getMaxHeightOpen();
        }else{
            float maxHeight = plant.getMaxHeightClosed(); 
        }

        float plantHeight = (2 / (1 + (Math.exp((age/lifeSpan)*q))) - 1) * maxHeight;
        // where t is the current tree age in years, tm is the maximum achievable tree age in years,
        // mh is maximum achieve height in metres.
        return plantHeight;
    }
}