
// this class is going to be a 2x2 grid tester with stated randomised values
// to test that we are cooking sufficiently
package plantsketch;

import java.util.*;
import java.util.function.Consumer;

public class TestGrid 
{

    boolean isTestGrid = true;
    private final Consumer<String> logger;

    // set values
    int sampleCount = 30;
    float gridSpacing = 25f; // in metres
    int dimX = 2;
    int dimY = 2; // dimensions of the grid 
    float openOrClosedDensity = 0.8f; // threshold for open or closed growth form


    // Species stuff
    Map<Species, SpeciesMap> mapBySpecies;
    List<Species> speciesList;
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
// arbitrary values for elev
    float minElev = 60.0f;
    float maxElev = 100.0f; // the problem with this is that the slop is often far too high
    // temp change with elevation? air?
    float minSlope = 0.0f; // flat
    float maxSlope = 85.0f; // 5 higher
// TO DO: CHANGE CODE TO HAVE OPTION TO DIRECTLY MANIPULATE SLOPE

    // abiotic maps
    List<PointSample> pinkNoise; // pink noise samples
    TemperatureMap temp;
    AgeMap age;
    MoistureMap moist;
    SunlightMap sun;
    Terrain terrain;
    AbioticFactors abiotics;


    // constructor for the 2x2 test grid
    public TestGrid(Consumer<String> logger, boolean isTestGrid, int sampleCount)
    {
        this.logger = (logger == null) ? (s -> {}) : logger;
        this.isTestGrid = isTestGrid;
        // gonna leave the testing grids at the preset sample count of 30
        if (!isTestGrid) this.sampleCount = sampleCount;
        
    }

// --------------------------
// simulation methods:

// used to be in constructor
    public void initialiseTest()
    {
        temp = new TemperatureMap(dimX, dimY, gridSpacing, null);
        age = new AgeMap(dimX, dimY, gridSpacing, null);
        moist = new MoistureMap(dimX, dimY, gridSpacing ,null);
        sun = new SunlightMap(dimX, dimY, gridSpacing, null);
        abiotics = new AbioticFactors(moist, temp, sun);
        terrain = new Terrain(dimX, dimY, gridSpacing, abiotics, makeRandomGrid(minElev, maxElev));

        SpeciesDictionary dict = new SpeciesDictionary();
        speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());
    }

    // random grid maker
    public float[][] makeRandomGrid(float min, float max)
    {
        float[][] grid = new float[dimX][dimY];
        Random r = new Random();
        for (int i = 0; i < dimX; i++) {
            for (int j = 0; j < dimY; j++) {
                grid[i][j] = min + r.nextFloat() * (max - min);
            }
        }
        return grid;
    }

    // this class makes random values for the grids for each map using the makeRandomGrid method above.
    // the random grids are made within the preset boundary values in teh instance variables above
    public void randomiseGridValues()
    {
        temp.setGrid(makeRandomGrid(minTemp, maxTemp));
        age.setGrid(makeRandomGrid(minAge, maxAge));
        moist.setGrid(makeRandomGrid(minMoist, maxMoist));
        sun.setGrid(makeRandomGrid(minSun, maxSun));
        terrain.setElevationGrid(makeRandomGrid(minElev, maxElev));
    }
    
    // Preset 1
    public void loadPreset1() {

        temp.setGrid(new float[][] {
            {8.0f, 7.0f},
            {8.0f, 7.0f}
        });

        age.setGrid(new float[][] {
            {300f, 300f},
            {300.0f, 300.0f}
        });

        moist.setGrid(new float[][] {
            {25.0f, 25.0f},
            {25.0f, 25.0f}
        });

        sun.setGrid(new float[][] {
            {6.0f, 7.0f},
            {5.5f, 6.0f}
        });

        terrain.setElevationGrid(new float[][] {
            {60.0f, 65.0f},
            {68.0f, 74.0f}
        });
        
        logger.accept("Loaded Preset 1: perfect conditions for most species");
    }
    
    // Preset 2
    public void loadPreset2() {

        temp.setGrid(new float[][] {
            {2f, 0f},
            {3.0f, 2f}
        });
        for(int x = 0 ; x < 2; x++)
        {
            for(int y = 0 ; y < 2; y++)
            {
                System.out.println(temp.getValue(x, y));     
            }
        }


        age.setGrid(new float[][] {
            {400.0f, 450.0f},
            {420.0f, 500.0f}
        });

        moist.setGrid(new float[][] {
            {22.0f, 8.0f},
            {16.0f, 9.0f}
        });

        sun.setGrid(new float[][] {
            {8f, 10f},
            {6f, 11f}
        });

        terrain.setElevationGrid(new float[][] {
            {60.0f, 87.0f},
            {65.0f, 90.0f}
        });

        logger.accept("Loaded Preset 2: harsh conditions");
    }



    // generate pink noise
    public List<PointSample> pinkNoise()
    {
        float metersX = dimX * gridSpacing;
        float metersY = dimY * gridSpacing;
        PinkNoiseSampler sampler = new PinkNoiseSampler(metersX, metersY, 2.0f, 0); // figure out what seed means and does
        List<PointSample> samples = sampler.generateSamples(sampleCount);
        this.pinkNoise = samples;
        return samples;
    }

    // how many pink noise samples were generated
    public int getNumPinkNoise()
    {
        return pinkNoise.size();
    }

    // clamp method
    private static int clamp(int v, int lo, int hi) {
        return (v < lo) ? lo : (v > hi ? hi : v);
    }

    // Placement loop
    public void placementLoop()
    {
        mapBySpecies = new LinkedHashMap<>();
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotics);
        Random r = new Random();
        int placed = 0;
        ArrayList<Species> candidates = new ArrayList<>();
        ArrayList<Table> wheel = new ArrayList<>();
// logic here potentially wrong
        currentPlants.clear();
// changed the wheel to an arraylist so that it is easier to size and also sumv was not being used

        for (PointSample s : pinkNoise) {
            int xCell = clamp((int) (s.getX() / gridSpacing), 0, dimX - 1);
            int yCell = clamp((int) (s.getY() / gridSpacing), 0, dimY - 1);

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

            if (!mapBySpecies.containsKey(chosen)) mapBySpecies.put(chosen, new SpeciesMap(chosen));

            SpeciesMap bucket = mapBySpecies.get(chosen);
            if (bucket != null) bucket.setPlantAt(p);
// why if != null?
        
        }

    }

    // assemble forest
    public Forest assembleForest()
    {
        forest = new Forest(dimX, dimY);
        mapBySpecies.values().forEach(forest::addSpeciesMap);
        numPlants = forest.getAllPlants().size();
        return forest;
    }

    // make simulation result object
    public SimulationResult makeSimResult()
    {
        simResult = new SimulationResult(forest, pinkNoise, dimX, dimY, gridSpacing, terrain.getElevationGrid());
        new EcoVizOutput(simResult).createFile("testingGrid.pdb"); // and then make the file
        return simResult;
// could potentially use my list of plants for this instead of the species maps
    }

// TO DO: FINISH THIS METHOD
// this method is for when the values are changed for the grid!!
    // Recalculate with same species positions
    // ignoring all of this hard work i reckon
    public void recalculateWithSameSpecies() {
        
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
            int xCell = clamp((int) (s.getX() / gridSpacing), 0, dimX - 1);
            int yCell = clamp((int) (s.getY() / gridSpacing), 0, dimY - 1);  

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
    public SimulationResult runChange(boolean pinkNoise) // , boolean updateSpecies
    {
        if (pinkNoise) {
            pinkNoise();
        }
            placementLoop();
            assembleForest();
            makeSimResult();
        /*
        if(!updateSpecies)
        {
            recalculateWithSameSpecies();
            assembleForest();
            makeSimResult();
        }
        */
        return simResult;
    }

    public SimulationResult run(int choice) 
    {
        if (isTestGrid)
        {
            initialiseTest();

            if (choice == 1) {
                loadPreset1();
            } else if (choice == 2) {
                loadPreset2();
            } else {
                randomiseGridValues();
            }

            pinkNoise();
            placementLoop();
            assembleForest();
            makeSimResult();

            return simResult;
        }
        
        else
        {
            if (choice == 1) {
                loadD("src/target/classes/D1-256");
            } else if (choice == 2) {
                loadD("src/target/classes/D2-512");
            } else if (choice == 3) {
                loadD("src/target/classes/D3-1024");
            } else if (choice == 4) {
                loadD("src/target/classes/D4-1024");
            } else if (choice == 5) {
                //chooseFolder();
            } else randomiseGridValues();

            pinkNoise();
            placementLoop();
            assembleForest();
            makeSimResult();

            return simResult;
        }
    }

    public void loadD(String file)
    {
        FileManager fm = new FileManager();
        fm.fileFinder(file);

        dimX = fm.getDimX();
        dimY = fm.getDimY();
        gridSpacing = fm.getGridSpacing();

        logger.accept("Dimensions: " + dimX + " × " + dimY + ", spacing " + gridSpacing + "m");

        temp = new TemperatureMap(dimX, dimY, gridSpacing, fm.getTemperatureGrid());
        age = new AgeMap(dimX, dimY, gridSpacing, fm.getAgeGrid());
        moist = new MoistureMap(dimX, dimY, gridSpacing ,fm.getMoistureGrid());
        sun = new SunlightMap(dimX, dimY, gridSpacing, fm.getSunlightGrid());
        abiotics = new AbioticFactors(moist, temp, sun);
        terrain = new Terrain(dimX, dimY, gridSpacing, abiotics, fm.getElevationGrid());

        SpeciesDictionary dict = new SpeciesDictionary();
        speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());

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
        return temp.getGrid();
    }

    public void setTemperatureGrid(float[][] currentTempGrid) {
        this.temp.setGrid(currentTempGrid);
    }

    public float[][] getAgeGrid() {
        return age.getGrid();
    }

    public void setAgeGrid(float[][] currentAgeGrid) {
        this.age.setGrid(currentAgeGrid);
    }

    public float[][] getMoistureGrid() {
        return moist.getGrid();
    }

    public void setMoistureGrid(float[][] currentMoistGrid) {
        this.moist.setGrid(currentMoistGrid);
    }

    public float[][] getSunlightGrid() {
        return sun.getGrid();
    }

    public void setSunlightGrid(float[][] currentSunGrid) {
        this.sun.setGrid(currentSunGrid);
    }

    public float[][] getElevationGrid() {
        return terrain.getElevationGrid();
    }

    public void setElevationGrid(float[][] currentElevGrid) {
        this.terrain.setElevationGrid(currentElevGrid);
    }

    public float[][] getSlopeGrid() {
        return terrain.getSlopeGrid();
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