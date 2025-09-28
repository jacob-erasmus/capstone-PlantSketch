package plantsketch;

import java.util.ArrayList;
import java.util.Random;

//uses a table class which stores cumulative viability for species
public class RouletteWheelSelector {
    private float random;
    private Random r = new Random();

    public RouletteWheelSelector() {
        this.random = 0.0f;
    }

    public RouletteWheelSelector(float sumViability) {
        this.random = (sumViability) * r.nextFloat();
    }

    public Species selectSpecies(ArrayList<Table> viabilityTable, int count) {
        // Method stub
        for (int i = 0; i < count; i++) {
            if (random < viabilityTable.get(i).getAddedViability())
            {
                return viabilityTable.get(i).getSpecies();
            }
        }
        return null;
    }
}