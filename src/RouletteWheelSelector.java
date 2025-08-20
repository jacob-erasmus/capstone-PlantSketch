import java.util.List;
import java.util.Random;

public class RouletteWheelSelector {
    private float random;
    private float density;
    private Random r = new Random();

    public RouletteWheelSelector() {
        this.random = 0.0f;
    }
    
    public RouletteWheelSelector(float density) {
        this.density = density;
        this.random = (density) * r.nextFloat();
    }
    
    public String selectSpecies(List<Species> speciesList) {
        // Method stub
        if (random > density){
            return "none";
        }
        int indexOfSpecies = -1;
        float closestProbability = 1000000000;
        for (int i = 0; i < speciesList.size(); i++){
            float viabililty = speciesList.get(i).getViabilityAtPoint();
            if (viabililty != 0){
                float closest = Math.abs(viabililty - random);
                if (closest < closestProbability){
                    indexOfSpecies = i;
                }
            }
        }
        if (indexOfSpecies == -1){
            return "none";
        }
        
        return speciesList.get(indexOfSpecies).getName();
    }
}