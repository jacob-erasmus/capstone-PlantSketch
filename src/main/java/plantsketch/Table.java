package plantsketch;

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
