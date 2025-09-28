package plantsketch;

/**
 * Table - Species Viability Data Holder
 *
 * Simple container that pairs a species with its cumulative viability score
 * at a specific location. Used in sampling and selection algorithms.
 */
public class Table {
    Species species;
    float addedViability;

    public Table(Species species, float addedViability) {
        this.species = species;
        this.addedViability = addedViability;
    }

    public float getAddedViability() {
        return addedViability;
    }

    public Species getSpecies() {
        return species;
    }
}
