package plantsketch;

import java.util.*;
import java.util.function.Consumer;

/**
 * Pure orchestration of one simulation:
 * - Loads grids from the given folder (via FileManager).
 * - Builds domain objects (AbioticFactors, Terrain, AgeMap).
 * - Generates pink-noise samples.
 * - Computes viability and places plants into species maps (no name-based switch).
 * Returns a SimulationResult DTO for the UI to render.
 */
public final class SimulationRunner {

    private final Consumer<String> logger;

    public SimulationRunner(Consumer<String> logger) {
        this.logger = (logger == null) ? (s -> {}) : logger;
    }

    public SimulationResult run(String folderPath, int sampleCount) {
        Objects.requireNonNull(folderPath, "folderPath");
        if (sampleCount <= 0) throw new IllegalArgumentException("sampleCount must be > 0");

        // 1) Load data
        FileManager fm = new FileManager();
        fm.fileFinder(folderPath); // prints its own "Loading ..." lines to System.out

        int dimX = fm.getDimX();
        int dimY = fm.getDimY();
        float gridSpacing = fm.getGridSpacing();

        logger.accept("Dimensions: " + dimX + " × " + dimY + ", spacing " + gridSpacing + "m");

        // 2) Domain objects
        TemperatureMap temperatureMap = new TemperatureMap(dimX, dimY, gridSpacing, fm.getTemperatureGrid());
        MoistureMap moistureMap = new MoistureMap(dimX, dimY, gridSpacing, fm.getMoistureGrid());
        SunlightMap sunlightMap = new SunlightMap(dimX, dimY, gridSpacing, fm.getSunlightGrid());
        AbioticFactors abiotic = new AbioticFactors(moistureMap, temperatureMap, sunlightMap);
        Terrain terrain = new Terrain(dimX, dimY, gridSpacing, abiotic, fm.getElevationGrid());
        AgeMap ageMap = new AgeMap(dimX, dimY, gridSpacing, fm.getAgeGrid());

        // 3) Pink noise samples
        float metersX = dimX * gridSpacing;
        float metersY = dimY * gridSpacing;
        PinkNoiseSampler sampler = new PinkNoiseSampler(metersX, metersY, 2.0f, 42L);
        List<PointSample> samples = sampler.generateSamples(sampleCount);
        logger.accept("Samples generated: " + samples.size());

        // 4) Species setup
        SpeciesDictionary dict = new SpeciesDictionary();
        List<Species> speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());

        Map<Species, SpeciesMap> mapBySpecies = new LinkedHashMap<>();
        for (Species sp : speciesList) {
            mapBySpecies.put(sp, new SpeciesMap(sp));
        }

        // 5) Placement loop
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotic);
        Random r = new Random();
        int placed = 0;
        ArrayList<Species> candidates = new ArrayList<>(speciesList.size());
        ArrayList<Table> wheel = new ArrayList<>();

        for (PointSample s : samples) {
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

            // roulette wheel on cumulative viability
            Species chosen = new RouletteWheelSelector(sumV).selectSpecies(wheel, wheel.size());
            if (chosen == null) continue;

            // age, height, canopy
            float cohortAge = ageMap.getAge(xCell, yCell);
            float cap = Math.min(cohortAge, chosen.getLifeSpan());
            float plantAge = r.nextFloat() * cap * chosen.getViabilityAtPoint();

            boolean isOpen = density > 0.8f;
            float height = new GrowthFunction().calculateSize(chosen, plantAge, isOpen);
            float canopy = height * (isOpen ? chosen.getRadiusMultiplierOpen() : chosen.getRadiusMultiplierClosed());

            Plant p = new Plant(++placed, chosen.getMnemonic(), s.getX(), s.getY(), plantAge, chosen, canopy, height, true,
                    chosen.getViabilityAtPoint(), isOpen);

            SpeciesMap bucket = mapBySpecies.get(chosen);
            if (bucket != null) bucket.setPlantAt(p);
        }

        // 6) Assemble forest
        Forest forest = new Forest(dimX, dimY);
        mapBySpecies.values().forEach(forest::addSpeciesMap);

        logger.accept("Plants placed: " + forest.getAllPlants().size());
        SimulationResult simResult = new SimulationResult(forest, samples, dimX, dimY, gridSpacing, fm.getElevationGrid());

// william isi adding here to print to .pdb file for ecoviz
        new EcoVizOutput(simResult).createFile("output.pdb");
// end of william

        return simResult;
    }

    private static int clamp(int v, int lo, int hi) {
        return (v < lo) ? lo : (v > hi ? hi : v);
    }
}
