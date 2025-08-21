
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
    
    public void setPlantAt(Plant plant) {
        // Method stub
        map.add(plant);
    }
    
    public boolean isPlantAt(float x, float y) {
        // Method stub
        for (int i = 0; i < map.size(); i++){
            if (map.get(i).getX() == x && map.get(i).getY() == y){
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<Plant> getPlants(){
        return map;
    }

    public Species getSpecies(){
        return species;
    }

    public void removePlantAt(float x, float y) {
        // Method stub
        for (int i = 0; i < map.size(); i++){
            if (map.get(i).getX() == x && map.get(i).getY() == y){
                map.remove(i);
                break;
            }
        }
    }
    
    public Plant getPlantAt(float x, float y) {
        // Method stub
        for (int i = 0; i < map.size(); i++){
            if (map.get(i).getX() == x && map.get(i).getY() == y){
                return map.get(i);
            }
        }
        return null;
    }
    
    public void clearMap() {
        // Method stub
        map.clear();
    }
}
