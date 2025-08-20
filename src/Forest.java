import java.util.ArrayList;

public class Forest {
    private ArrayList<Plant> plants;
    private ArrayList<Species> species;
    private ArrayList<SpeciesMap> speciesMap;
    
    public Forest() {
        this.plants = new ArrayList<>();
        this.species = new ArrayList<>();
        this.speciesMap = new ArrayList<>();
    }
    
    public Forest(int width, int height) {
        this.plants = new ArrayList<>();
        this.species = new ArrayList<>();
        this.speciesMap = new ArrayList<>();
    }
    
    public void addSpeciesMap(SpeciesMap speciesMap){
        this.speciesMap.add(speciesMap);
    }

    public void addPlant(Plant plant) {
        // Method stub
    }
    
    public void removePlant(Plant plant) {
        // Method stub
    }
    
    public ArrayList<Plant> getPlants() {
        // Method stub
        return new ArrayList<>();
    }
    
    public ArrayList<Species> getSpecies() {
        // Method stub
        return new ArrayList<>();
    }
    
    public void addSpecies(Species species) {
        // Method stub
    }
    
    public SpeciesMap getSpeciesMap(Species species) {
        // Method stub
        return null;
    }
    
    public void applyBrushChange(Brush brush) {
        // Method stub
    }
    
    public void regenerateForest() {
        // Method stub
    }
}
