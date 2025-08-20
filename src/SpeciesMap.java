
import java.util.ArrayList;

public class SpeciesMap {
    private Species species;
    private ArrayList<Plant> map;
    
    public SpeciesMap() {
        this.species = null;
        this.map = new ArrayList<>();
    }
    
    public SpeciesMap(Species species) {
        this.species = species;
        this.map = new ArrayList<>();
    }
    
    public void setPlantAt(Plant plant, int dimX, int dimY) {
        // Method stub
    }
    
    public boolean isPlantAt(float x, float y) {
        // Method stub
        return false;
    }
    
    public void removePlantAt(float x, float y) {
        // Method stub
    }
    
    public Plant getPlantAt(float x, float y) {
        // Method stub
        return null;
    }
    
    public void clearMap() {
        // Method stub
    }
}
