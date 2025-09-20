
// this class is going to be a 2x2 grid tester with stated randomised values
// to test that we are cooking sufficiently
package plantsketch;

import java.util.*;
import java.util.function.Consumer;

public class TestGrid 
{

    private final Consumer<String> logger;

    // set values
    int sampleCount = 30;
    float gridSpacing = 2.5f; // in metres
    int dim = 2; // dimensions of the grid (square)
    float openOrClosedDensity = 0.8f; // threshold for open or closed growth form


    // Species stuff
    List<Species> speciesList;
    Map<Species, SpeciesMap> mapBySpecies;
    Forest forest;
    SimulationResult simResult;

    // Store current plants for recalculation
    private List<Plant> currentPlants = new ArrayList<>();

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
    float minElev = 0.0f;
    float maxElev = 0.0f;
    // temp change with elevation? air?
    float minSlope = 0.0f; // flat
    float maxSlope = 85.0f; // 5 higher
// TO DO: CHANGE CODE TO HAVE OPTION TO DIRECTLY MANIPULATE SLOPE

    // Current grid values
    private float[][] currentTempGrid;
    private float[][] currentAgeGrid;
    private float[][] currentMoistGrid;
    private float[][] currentSunGrid;
    private float[][] currentElevGrid;
    private float[][] currentSlopeGrid;

    // abiotic maps
    List<PointSample> pinkNoise; // pink noise samples
    TemperatureMap temp;
    AgeMap age;
    MoistureMap moist;
    SunlightMap sun;
    Terrain terrain;
    AbioticFactors abiotics;


    // constructor
    public TestGrid(Consumer<String> logger)
    {
        this.logger = (logger == null) ? (s -> {}) : logger;
        temp = new TemperatureMap(dim, dim, gridSpacing, null);
        age = new AgeMap(dim, dim, gridSpacing, null);
        moist = new MoistureMap(dim, dim, gridSpacing ,null);
        sun = new SunlightMap(dim, dim, gridSpacing, null);
        abiotics = new AbioticFactors(moist, temp, sun);
        terrain = new Terrain(dim, dim, gridSpacing, abiotics, currentElevGrid = makeRandomGrid(minElev, maxElev));
    }


// --------------------------
// simulation methods:

    // random grid maker
    public float[][] makeRandomGrid(float min, float max)
    {
        float[][] grid = new float[dim][dim];
        Random r = new Random();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                grid[i][j] = min + r.nextFloat() * (max - min);
            }
        }
        return grid;
    }

    // this class makes random values for the grids for each map using the makeRandomGrid method above.
    // the random grids are made within the preset boundary values in teh instance variables above
    public void randomiseGridValues()
    {
        temp.setGrid(currentTempGrid = makeRandomGrid(minTemp, maxTemp));
        age.setGrid(currentAgeGrid = makeRandomGrid(minAge, maxAge));
        moist.setGrid(currentMoistGrid = makeRandomGrid(minMoist, maxMoist));
        sun.setGrid(currentSunGrid = makeRandomGrid(minSun, maxSun));
        terrain.setElevationGrid(currentElevGrid = makeRandomGrid(minElev, maxElev));
// TO DO: WOULD BE BETTER TO DIRECTLY MANIPULATE SLOPE
    }
    
    // Preset 1
    public void loadPreset1() {
        currentTempGrid = new float[][] {
            {8.0f, 12.0f},
            {9.0f, 11.0f}
        };
        temp.setGrid(currentTempGrid);
        currentAgeGrid = new float[][] {
            {50.0f, 75.0f},
            {60.0f, 80.0f}
        };
        age.setGrid(currentAgeGrid);
        currentMoistGrid = new float[][] {
            {35.0f, 40.0f},
            {38.0f, 42.0f}
        };
        moist.setGrid(currentMoistGrid);
        currentSunGrid = new float[][] {
            {8.0f, 9.0f},
            {7.5f, 8.5f}
        };
        sun.setGrid(currentSunGrid);
        currentElevGrid = new float[][] {
            {20.0f, 25.0f},
            {22.0f, 28.0f}
        };
        terrain.setElevationGrid(currentElevGrid);

        // idk if this one works or if I should do this later as idrk when the slopes are made
        currentSlopeGrid = terrain.getSlopeGrid();
        
        logger.accept("Loaded Preset 1: ");
    }
    
    // Preset 2
    public void loadPreset2() {

        currentTempGrid = new float[][] {
            {20f, 22.5f},
            {19.0f, 21f}
        };
        temp.setGrid(currentTempGrid);
        currentAgeGrid = new float[][] {
            {400.0f, 450.0f},
            {420.0f, 500.0f}
        };
        age.setGrid(currentAgeGrid);
        currentMoistGrid = new float[][] {
            {25.0f, 25.0f},
            {25.0f, 25.0f}
        };
        moist.setGrid(currentMoistGrid);
        currentSunGrid = new float[][] {
            {7f, 7.5f},
            {5.0f, 6f}
        };
        sun.setGrid(currentSunGrid);
        currentElevGrid = new float[][] {
            {60.0f, 70.0f},
            {65.0f, 75.0f}
        };
        terrain.setElevationGrid(currentElevGrid);

        currentSlopeGrid = terrain.getSlopeGrid();

        logger.accept("Loaded Preset 2:");
    }


    // generate pink noise
    public List<PointSample> pinkNoise()
    {
        float meters = dim * gridSpacing;
        PinkNoiseSampler sampler = new PinkNoiseSampler(meters, meters, 2.0f, 0); // figure out what seed means and does
        List<PointSample> samples = sampler.generateSamples(sampleCount);
        this.pinkNoise = samples;
        return samples;
    }

    // how many pink noise samples were generated
    public int getNumPinkNoise()
    {
        return pinkNoise.size();
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
        ArrayList<Species> candidates = new ArrayList<>();
        ArrayList<Table> wheel = new ArrayList<>();
// logic here potentially wrong
        currentPlants.clear();
        mapBySpecies.values().forEach(SpeciesMap::clearMap);
// changed the wheel to an arraylist so that it is easier to size and also sumv was not being used

        for (PointSample s : pinkNoise) {
            int xCell = clamp((int) (s.getX() / gridSpacing), 0, dim - 1);
            int yCell = clamp((int) (s.getY() / gridSpacing), 0, dim - 1);

            float density = 0f;
            float sumV = 0f;
            candidates.clear();
            wheel.clear();

            for (Species sp : speciesList) {
                float v = calc.viabililty(sp, xCell, yCell);
                sp.setViabilityAtPoint(v);
                if (v > 0f) {
                    candidates.add(sp);
                    density = Math.max(density, v);
                    sumV += v;
                    wheel.add(new Table(sp, sumV));
                }
            }
            if (candidates.isEmpty()) continue;

            // thinning by density
            if (density < r.nextFloat()) continue;
// changed > to < because if the float is greater than the density the plant can't exist

            // roulette wheel on cumulative viability
            
            Species chosen = new RouletteWheelSelector(sumV).selectSpecies(wheel, wheel.size());
            if (chosen == null) continue;

            float cohortAge = age.getAge(xCell, yCell);
            float cap = Math.min(cohortAge, chosen.getLifeSpan());
            float plantAge = r.nextFloat() * cap * chosen.getViabilityAtPoint();

            boolean isOpen = density > openOrClosedDensity;
            float height = new GrowthFunction().calculateSize(chosen, plantAge, isOpen);
            float canopy = height * (isOpen ? chosen.getRadiusMultiplierOpen() : chosen.getRadiusMultiplierClosed());
// trusting the above equations

            Plant p = new Plant(++placed, chosen.getMnemonic(), s.getX(), s.getY(), plantAge, chosen, canopy, height, true,
                    chosen.getViabilityAtPoint(), isOpen);
            currentPlants.add(p);
// the plants weren't getting added to the list of total plants

            SpeciesMap bucket = mapBySpecies.get(chosen);
            if (bucket != null) bucket.setPlantAt(p);
// why if != null?
        
        }

    }

    // assemble forest
    public Forest assembleForest()
    {
        forest = new Forest(dim, dim);
        mapBySpecies.values().forEach(forest::addSpeciesMap);
        numPlants = forest.getAllPlants().size();
        return forest;
    }

    // make simulation result object
    public SimulationResult makeSimResult()
    {
        simResult = new SimulationResult(forest, pinkNoise, dim, dim, gridSpacing, terrain.getElevationGrid());
        new EcoVizOutput(simResult).createFile("testingGrid.pdb"); // and then make the file
        return simResult;
// could potentially use my list of plants for this instead of the species maps
    }

// TO DO: FINISH THIS METHOD
// this method is for when the values are changed for the grid!!
    // Recalculate with same species positions
    public void recalculateWithSameSpecies() {
    
        
        // Clear old species maps and forest
        setupSpeciesMap();

        
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotics);
        Random r = new Random();
        int placed = 0;
        
        // Store the old plants temporarily
        List<Plant> oldPlants = new ArrayList<>();
        if (forest != null) {
            oldPlants.addAll(currentPlants);
        }
        currentPlants.clear();
                // logic here potentially wrong
        mapBySpecies.values().forEach(SpeciesMap::clearMap);
        
        // Re-process each pink noise point with same species
        for (Plant s : oldPlants) {
            int xCell = clamp((int) (s.getX() / gridSpacing), 0, dim - 1);
            int yCell = clamp((int) (s.getY() / gridSpacing), 0, dim - 1);
            
            // Find if there was a plant at this location
            /*
            Plant oldPlant = null;
            for (Plant p : oldPlants) {
                if (Math.abs(p.getX() - s.getX()) < 0.01 && Math.abs(p.getY() - s.getY()) < 0.01) {
                    oldPlant = p;
                    break;
                }
            }
        
            
            if (oldPlant == null) continue;
              */          

            Species species = s.getSpecies();
            // Recalculate viability with new conditions
            float v = calc.viabililty(species, xCell, yCell);
            //if (v < r.nextFloat()) continue;

            // Recalculate age and growth parameters
            float cohortAge = age.getAge(xCell, yCell);
            float cap = Math.min(cohortAge, species.getLifeSpan());
            float plantAge = Math.min(s.getAge(), cap * v); // Adjust age based on new viability
            
            boolean isOpen = v > openOrClosedDensity;
            float height = new GrowthFunction().calculateSize(species, plantAge, isOpen);
            float canopy = height * (isOpen ? species.getRadiusMultiplierOpen() : species.getRadiusMultiplierClosed());
            
            // Create updated plant
            Plant p = new Plant(++placed, species.getMnemonic(), s.getX(), s.getY(), 
                    plantAge, species, canopy, height, true, v, isOpen);
            
            SpeciesMap bucket = mapBySpecies.get(species);
            if (bucket != null) bucket.setPlantAt(p);
        }
        
        // Rebuild forest
        assembleForest();
        logger.accept("Recalculated " + placed + " plants with same species positions");
    }

    // add setters to change the values of the maps

    // add setters to change viabilities of species and stuff

    // be able to select species and stuff and turn on and off

    // run method 
    public SimulationResult runChange(boolean pinkNoise, boolean updateSpecies)
    {
        if (pinkNoise) {
            pinkNoise();
        }
        if (updateSpecies) {

            placementLoop();
            assembleForest();
            makeSimResult();
        }
        else
        {
            recalculateWithSameSpecies();
            assembleForest();
            makeSimResult();
        }
        return simResult;
    }

    public SimulationResult run(int choice) 
    {
        if (choice == 1) {
            loadPreset1();
        } else if (choice == 2) {
            loadPreset2();
        } else {
            randomiseGridValues();
        }

        setupSpecies();
        setupSpeciesMap();
        pinkNoise();
        placementLoop();
        assembleForest();
        makeSimResult();

        return simResult;
    }












// --------------------------
// getters and setters:
    public List<PointSample> getPinkNoise() {
    return pinkNoise;
    }

    public void setPinkNoise(List<PointSample> pinkNoise) {
        this.pinkNoise = pinkNoise;
    }

    public TemperatureMap getTemp() {
        return temp;
    }

    public void setTemp(TemperatureMap temp) {
        this.temp = temp;
    }

    public AgeMap getAge() {
        return age;
    }

    public void setAge(AgeMap age) {
        this.age = age;
    }

    public MoistureMap getMoist() {
        return moist;
    }

    public void setMoist(MoistureMap moist) {
        this.moist = moist;
    }

    public SunlightMap getSun() {
        return sun;
    }

    public void setSun(SunlightMap sun) {
        this.sun = sun;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public AbioticFactors getAbiotics() {
        return abiotics;
    }

    public void setAbiotics(AbioticFactors abiotics) {
        this.abiotics = abiotics;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(float minTemp) {
        this.minTemp = minTemp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(float maxTemp) {
        this.maxTemp = maxTemp;
    }

    public float getMinAge() {
        return minAge;
    }

    public void setMinAge(float minAge) {
        this.minAge = minAge;
    }

    public float getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(float maxAge) {
        this.maxAge = maxAge;
    }

    public float getMinMoist() {
        return minMoist;
    }

    public void setMinMoist(float minMoist) {
        this.minMoist = minMoist;
    }

    public float getMaxMoist() {
        return maxMoist;
    }

    public void setMaxMoist(float maxMoist) {
        this.maxMoist = maxMoist;
    }

    public float getMinSun() {
        return minSun;
    }

    public void setMinSun(float minSun) {
        this.minSun = minSun;
    }

    public float getMaxSun() {
        return maxSun;
    }

    public void setMaxSun(float maxSun) {
        this.maxSun = maxSun;
    }

    public float getMinElev() {
        return minElev;
    }

    public void setMinElev(float minElev) {
        this.minElev = minElev;
    }

    public float getMaxElev() {
        return maxElev;
    }

    public void setMaxElev(float maxElev) {
        this.maxElev = maxElev;
    }

    public float getMinSlope() {
        return minSlope;
    }

    public void setMinSlope(float minSlope) {
        this.minSlope = minSlope;
    }

    public float getMaxSlope() {
        return maxSlope;
    }

    public void setMaxSlope(float maxSlope) {
        this.maxSlope = maxSlope;
    }

    public float[][] getTemperatureGrid() {
        return currentTempGrid;
    }

    public void setTemperatureGrid(float[][] currentTempGrid) {
        this.currentTempGrid = currentTempGrid;
    }

    public float[][] getAgeGrid() {
        return currentAgeGrid;
    }

    public void setAgeGrid(float[][] currentAgeGrid) {
        this.currentAgeGrid = currentAgeGrid;
    }

    public float[][] getMoistureGrid() {
        return currentMoistGrid;
    }

    public void setMoistureGrid(float[][] currentMoistGrid) {
        this.currentMoistGrid = currentMoistGrid;
    }

    public float[][] getSunlightGrid() {
        return currentSunGrid;
    }

    public void setSunlightGrid(float[][] currentSunGrid) {
        this.currentSunGrid = currentSunGrid;
    }

    public float[][] getElevationGrid() {
        return currentElevGrid;
    }

    public void setElevationGrid(float[][] currentElevGrid) {
        this.currentElevGrid = currentElevGrid;
    }

    public float[][] getSlopeGrid() {
        return currentSlopeGrid;
    }

    public void setSlopeGrid(float[][] currentSlopeGrid) {
        this.currentSlopeGrid = currentSlopeGrid;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public int getNumPlants() {
        return numPlants;
    }   

    public void setNumPlants(int numPlants) {
        this.numPlants = numPlants;
    }
}