
// this class is going to be a 2x2 grid tester with stated randomised values
// to test that we are cooking sufficiently
package plantsketch;

import java.util.*;
import java.util.function.Consumer;

public class TestGrid 
{
    // set values
    int sampleCount = 10;
    float gridSpacing = 2.5f; // in metres
    int dim = 2; // dimensions of the grid (square)

    // abiotic maps
    List<PointSample> pinkNoise; // pink noise samples
    TemperatureMap temp;
    AgeMap age;
    MoistureMap moist;
    SunlightMap sun;
    Terrain terrain;
    AbioticFactors abiotics;

    // Species stuff
    List<Species> speciesList;
    Map<Species, SpeciesMap> mapBySpecies;
    Forest forest;
    SimulationResult simResult;

    // things to count
    int numPlants;

    // Preset Boundary values (based on ranges from literature)
    float minTemp = -2.0f; // 1.5 lower than coldest tree
    float maxTemp = 13.0f; // 1 higher
    float minAge = 0.0f; // no trees
    float maxAge = 650.0f; // 50 higher than oldest species
    float minMoist = 0.0f; // desert asf
    float maxMoist = 55.0f; // 5 higher
    float minSun = 0.0f; // no sun
    float maxSun = 13.0f; // 1 higher
    float minElev = 10.0f;
    float maxElev = 40.0f;
    // temp change with elevation? air?
    float minSlope = 0.0f; // flat
    float maxSlope = 85.0f; // 5 higher
// TO DO: CHANGE CODE TO HAVE OPTION TO DIRECTLY MANIPULATE SLOPE


    // constructor
    public TestGrid()
    {
        // initialise the maps

// TO DO: RANDOMISE GRID VALUES WITHIN REQUIRED RANGES
        temp = new TemperatureMap(dim, dim, gridSpacing, new float[][] { { 15.0f, 20.0f }, { 25.0f,30.0f } });
        age = new AgeMap(dim, dim, gridSpacing, new float[][] { { 1.0f, 2.0f }, { 3.0f, 4.0f } });
        moist = new MoistureMap(dim, dim, gridSpacing, new float[][] { { 0.1f, 0.2f }, { 0.3f, 0.4f } });
        sun = new SunlightMap(dim, dim, gridSpacing, new float[][] { { 100.0f, 200.0f }, { 300.0f, 400.0f } });
        abiotics = new AbioticFactors(moist, temp, sun);
        terrain = new Terrain(dim, dim, gridSpacing, abiotics, new float[][] { { 10.0f, 20.0f }, { 30.0f, 40.0f } });
    }


// --------------------------
// simulation methods:

    // generate pink noise
    public List<PointSample> pinkNoise()
    {
        float meters = dim * gridSpacing;
        PinkNoiseSampler sampler = new PinkNoiseSampler(meters, meters, 2.0f, 42L); // figure out what seed means and does
        List<PointSample> samples = sampler.generateSamples(sampleCount);
        this.pinkNoise = samples;
        return samples;
    }

    // how many pink noise samples were generated
    public int getNumPinkNoise()
    {
        return pinkNoise.size();
    }

    // the percentage of samples that were turned into points
    public int getPinkProportion()
    {
        return getNumPinkNoise() / sampleCount;
    }

    // setup list containing all species
    public List<Species> setupSpecies()
    {
        SpeciesDictionary dict = new SpeciesDictionary();
        speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());
        return speciesList;
    }

    // setup species map
    public Map<Species, SpeciesMap> setupSpeciesMap()
    {
        mapBySpecies = new LinkedHashMap<>();
        for (Species sp : speciesList) {
            mapBySpecies.put(sp, new SpeciesMap(sp));
        }
        return mapBySpecies;
    }

    // clamp method
        private static int clamp(int v, int lo, int hi) {
        return (v < lo) ? lo : (v > hi ? hi : v);
    }

    // Placement loop
    public void placementLoop()
    {
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotics);
        Random r = new Random();
        int placed = 0;
        ArrayList<Species> candidates = new ArrayList<>(speciesList.size());

        for (PointSample s : pinkNoise) {
            int xCell = clamp((int) (s.getX() / gridSpacing), 0, dim - 1);
            int yCell = clamp((int) (s.getY() / gridSpacing), 0, dim - 1);

            float density = 0f;
            float sumV = 0f;
            candidates.clear();

            for (Species sp : speciesList) {
                float v = calc.viabililty(sp, xCell, yCell);
                sp.setViabilityAtPoint(v);
                if (v > 0f) {
                    candidates.add(sp);
                    density = Math.max(density, v);
                    sumV += v;
                }
            }
            if (candidates.isEmpty()) continue;

            // thinning by density
            if (density > r.nextFloat()) continue;

            // roulette wheel on cumulative viability
            Table[] wheel = new Table[candidates.size()];
            float cumulative = 0f;
            for (int i = 0; i < candidates.size(); i++) {
                cumulative += candidates.get(i).getViabilityAtPoint();
                wheel[i] = new Table(candidates.get(i), cumulative);
            }
            Species chosen = new RouletteWheelSelector(cumulative).selectSpecies(wheel, wheel.length);
            if (chosen == null) continue;

            // age, height, canopy
            float cohortAge = age.getAge(xCell, yCell);
            float cap = Math.min(cohortAge, chosen.getLifeSpan());
            float plantAge = r.nextFloat() * cap * chosen.getViabilityAtPoint();

            boolean isOpen = density > 0.8f;
            float height = new GrowthFunction().calculateSize(chosen, plantAge, isOpen);
            float canopy = height * (isOpen ? chosen.getRadiusMultiplierOpen() : chosen.getRadiusMultiplierClosed());

            Plant p = new Plant(++placed, s.getX(), s.getY(), plantAge, chosen, canopy, height, true,
                    chosen.getViabilityAtPoint(), isOpen);

            SpeciesMap bucket = mapBySpecies.get(chosen);
            if (bucket != null) bucket.setPlantAt(p);
        }

    }

    // assemble forest
    public Forest assembleForest()
    {
        forest = new Forest(dim, dim);
        mapBySpecies.values().forEach(forest::addSpeciesMap);
        /*
        for (SpeciesMap sm : mapBySpecies.values()) {
            for (Plant p : sm.getPlants()) {
                forest.addPlant(p);
            }
        }
        */
        numPlants = forest.getAllPlants().size();
        return forest;
    }

    // make simulation result object
    public SimulationResult makeSimResult()
    {
        simResult = new SimulationResult(forest, pinkNoise, dim, dim, gridSpacing, terrain.getElevationGrid());
        return simResult;
    }

// --------------------------
// testing methods:

    // add methods that have pre-loaded values to test the maps

    // add setters to change the values of the maps

    // add setters to change viabilities of species and stuff

    // be able to select species and stuff and turn on and off

    // run method 



}