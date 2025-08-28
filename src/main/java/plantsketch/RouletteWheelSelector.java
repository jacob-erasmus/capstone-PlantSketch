package plantsketch;

import java.util.List;
import java.util.Random;

public class RouletteWheelSelector {
    private float random;
    private float sumViabilites;
    private Random r = new Random();

    public RouletteWheelSelector() {
        this.random = 0.0f;
    }

    public RouletteWheelSelector(float sumViabilites) {
        this.sumViabilites = sumViabilites;
        this.random = (sumViabilites) * r.nextFloat();
    }

    public Species selectSpecies(Table[] rouletteTable, int count) {
        // Method stub
        for (int i = 0; i < count; i++) {
            if (random > rouletteTable[i].getAddedViability()) {
            } else {
                return rouletteTable[i].getSpecies();
            }
        }
        return null;
    }
}