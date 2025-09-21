package plantsketch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Forest {
    private final ArrayList<SpeciesMap> OverallSpeciesMap;
    public HashMap<String, SpeciesMap> removedSpecies = new HashMap<>();

    public Forest() {
        this.OverallSpeciesMap = new ArrayList<>();
    }

    public Forest(int width, int height) {
        this.OverallSpeciesMap = new ArrayList<>();
    }

    public void addSpeciesMap(SpeciesMap speciesMap) {
        this.OverallSpeciesMap.add(speciesMap);
    }

    public void addSpeciesMapByName(String speciesName) {
        this.OverallSpeciesMap.add(removedSpecies.get(speciesName));
        removedSpecies.remove(speciesName);
    }

    // for local changes
    public void addPlant(Plant plant) {
        OverallSpeciesMap.stream()
            .filter(sm -> sm.getSpecies() == plant.getSpecies())
            .findFirst()
            .ifPresent(sm -> sm.setPlantAt(plant));
    }

    // for local changes
public void removePlant(Plant plant) {
    OverallSpeciesMap.stream()
        .filter(sm -> sm.getSpecies() == plant.getSpecies())
        .findFirst()
        .ifPresent(sm -> sm.removePlantAt(plant.getX(), plant.getY()));
}

public List<Plant> getAllPlants() {
    return OverallSpeciesMap.stream()
            .flatMap(sm -> sm.getPlants().stream())
            .toList(); // Java 16+
}

    // global change
    public void removeSpecies(String speciesName) {
        removedSpecies.put(speciesName, getSpeciesMap(speciesName));
        OverallSpeciesMap.remove(getSpeciesMap(speciesName));
    }

    public SpeciesMap getSpeciesMap(String speciesName) {
        // Method stub
        for (int i = 0; i < 7; i++) {
            if (OverallSpeciesMap.get(i).getSpecies().getName() == speciesName) {
                return OverallSpeciesMap.get(i);
            }
        }
        return null;
    }

    public ArrayList<SpeciesMap> getOverallSpeciesMap() {
        return OverallSpeciesMap;
    }

    public void applyBrushChange(Brush brush) {
        // Method stub
    }

    public void regenerateForest() {
        // Method stub

    }

      public List<SpeciesMap> getSpeciesMapList() {
        return Collections.unmodifiableList(OverallSpeciesMap);
    }
}
