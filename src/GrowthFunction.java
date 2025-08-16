public class GrowthFunction {
    private float maxSize;    //what is this for?
    private float growthRate;    //what is this for?
    
    public GrowthFunction() {
        this.maxSize = 1.0f;    //what is this for?
        this.growthRate = 0.1f;     //what is this for?
    }
    
    public GrowthFunction(float maxSize, float growthRate) {    //confused as why these parameters need?
        this.maxSize = maxSize;
        this.growthRate = growthRate;
    }
    
    public float calculateSize(Plant plant, boolean open) {
        float maxHeight;
        if (open){
            maxHeight = plant.getMaxHeightOpen();
        }else{
            maxHeight = plant.getMaxHeightClosed(); 
        }
        // h = ((2 / 1 + e^((t/tm)*q)) - 1) * mh
        // where t is the current tree age in years, tm is the maximum achievable tree age in years,
        // mh is maximum achieve height in metres.
        double height = ((2/(1 + Math.pow(Math.E, (plant.getAge()/plant.getLifeSpan())*plant.getQ())) - 1) * maxHeight);
        return (float)height;
    }
    
    public int getMaxAge() {    //what is this method for?
        return 0;
    }
}