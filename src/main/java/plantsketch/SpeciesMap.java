package plantsketch;

import java.util.ArrayList;

/**
 * Holds a  collection of plants for a specific species.
 * Provides operations to add, remove, and search for plants by location.
 */
public class SpeciesMap {
    private Species species;
    private ArrayList<Plant> map;

    /**
     * Creates an empty species map with no associated species.
     */
    public SpeciesMap() {
        this.species = null;
        this.map = new ArrayList<>();
    }

    /**
     * Creates an empty species map for the specified species.
     * @param species the species this map will contain
     */
    public SpeciesMap(Species species) {
        this.species = species;
        this.map = new ArrayList<>();
    }

    /**
     * Adds a plant to the map at its current position.
     * @param plant the plant to add to the map
     */
    public void setPlantAt(Plant plant) {
        map.add(plant);
    }

    /**
     * Checks if a plant exists at the specified coordinates.
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return true if a plant exists at the given location, false otherwise
     */
    public boolean isPlantAt(float x, float y) {
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i).getX() == x && map.get(i).getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of all plants in this species map.
     * @return ArrayList containing all plants in the map
     */
    public ArrayList<Plant> getPlants() {
        return map;
    }

    /**
     * Returns the species associated with this map.
     * @return the species, or null if no species is set
     */
    public Species getSpecies() {
        return species;
    }

    /**
     * Removes the first plant found at the specified coordinates.
     * @param x the x-coordinate of the plant to remove
     * @param y the y-coordinate of the plant to remove
     */
    public void removePlantAt(float x, float y) {
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i).getX() == x && map.get(i).getY() == y) {
                map.remove(i);
                break;
            }
        }
    }

    /**
     * Retrieves the first plant found at the specified coordinates.
     * @param x the x-coordinate to search
     * @param y the y-coordinate to search
     * @return the plant at the given location, or null if no plant exists there
     */
    public Plant getPlantAt(float x, float y) {
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i).getX() == x && map.get(i).getY() == y) {
                return map.get(i);
            }
        }
        return null;
    }

    /**
     * Removes all plants from the map.
     */
    public void clearMap() {
        map.clear();
    }
}
