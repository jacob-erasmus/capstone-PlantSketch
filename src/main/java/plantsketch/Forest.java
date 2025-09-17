package plantsketch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Forest {
    private final ArrayList<Plant> plants;
    private final ArrayList<SpeciesMap> speciesMap;

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
    speciesMap.stream()
        .filter(sm -> sm.getSpecies() == plant.getSpecies())
        .findFirst()
        .ifPresent(sm -> sm.setPlantAt(plant));
}

    // for local changes
public void removePlant(Plant plant) {
    speciesMap.stream()
        .filter(sm -> sm.getSpecies() == plant.getSpecies())
        .findFirst()
        .ifPresent(sm -> sm.removePlantAt(plant.getX(), plant.getY()));
}

public List<Plant> getAllPlants() {
    return speciesMap.stream()
            .flatMap(sm -> sm.getPlants().stream())
            .toList(); // Java 16+
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

      public List<SpeciesMap> getSpeciesMapList() {
        return Collections.unmodifiableList(speciesMap);
    }
}
