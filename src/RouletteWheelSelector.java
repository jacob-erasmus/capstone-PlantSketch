import java.util.List;
import java.util.Random;

public class RouletteWheelSelector {
    private float random;
    
    public RouletteWheelSelector() {
        this.random = 0.0f;
    }
    
    public RouletteWheelSelector(float density) {
        Random r = new Random();
        this.random = (density) * r.nextFloat();
    }
    
    public String selectSpecies(List<Species> speciesList) {
        // Method stub
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