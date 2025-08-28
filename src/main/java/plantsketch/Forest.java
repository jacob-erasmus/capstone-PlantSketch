package plantsketch;

import java.util.ArrayList;

public class Forest {
    private ArrayList<Plant> plants;
    private ArrayList<SpeciesMap> speciesMap;

    public Forest() {
        this.plants = new ArrayList<>();
        this.speciesMap = new ArrayList<>();
    }

    public Forest(int width, int height) {
        this.plants = new ArrayList<>();
        this.speciesMap = new ArrayList<>();
    }

    public void addSpeciesMap(SpeciesMap speciesMap) {
        this.speciesMap.add(speciesMap);
    }

    // for local changes
    public void addPlant(Plant plant) {
        for (int i = 0; i < 7; i++) {
            if (speciesMap.get(i).getSpecies() == plant.getSpecies()) {
                speciesMap.get(i).setPlantAt(plant);
                break;
            }
        }
    }

    // for local changes
    public void removePlant(Plant plant) {
        for (int i = 0; i < 7; i++) {
            if (speciesMap.get(i).getSpecies() == plant.getSpecies()) {
                speciesMap.get(i).setPlantAt(plant);
                break;
            }
        }
    }

    public ArrayList<Plant> getAllPlants() {
        for (int i = 0; i < 7; i++) {
            plants.addAll(speciesMap.get(i).getPlants());
        }
        return plants;
    }

    // global change
    public void removeSpecies(Species species) {
        speciesMap.remove(getSpeciesMap(species));
    }

    public SpeciesMap getSpeciesMap(Species species) {
        // Method stub
        for (int i = 0; i < 7; i++) {
            if (speciesMap.get(i).getSpecies() == species) {
                return speciesMap.get(i);
            }
        }
        return null;
    }

    public void applyBrushChange(Brush brush) {
        // Method stub
    }

    public void regenerateForest() {
        // Method stub

    }
}
