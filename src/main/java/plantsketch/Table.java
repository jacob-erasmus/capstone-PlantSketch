package plantsketch;

//The table class is used to hold a cumulative viability and a species for a point

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
