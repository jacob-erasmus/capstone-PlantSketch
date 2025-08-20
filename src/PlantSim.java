import javax.swing.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class PlantSim {
    private Terrain terrain;
    private Forest forest;
    private Renderer2D renderer;
    private UIController uiController;
    private FileManager fileManager;
    private boolean isRunning;
    private SunlightMap sunlightMap;
    private MoistureMap moistureMap;
    private TemperatureMap temperatureMap;
    
    
    public PlantSim() {
        this.terrain = null;
        this.forest = null;
        this.renderer = null;
        this.uiController = null;
        this.fileManager = null;
        this.isRunning = false;
    }
    
    public PlantSim(Terrain terrain, Forest forest, Renderer2D renderer, UIController uiController, FileManager fileManager) {
        this.terrain = terrain;
        this.forest = forest;
        this.renderer = renderer;
        this.uiController = uiController;
        this.fileManager = fileManager;
        this.isRunning = false;
    }
    

    public static void main(String[] args)
    {
        // option 1: upload all files
        // 2: upload .pdb file
        // read in files
        FileManager fileManager = new FileManager();
        fileManager.fileFinder();
        int dimX = fileManager.getDimX();
        int dimY = fileManager.getDimY();
        float gridSpacing = fileManager.getGridSpacing();

        // create the necessary objects that were read in from the files
        TemperatureMap temperatureMap = new TemperatureMap(dimX, dimY, gridSpacing, fileManager.getTemperatureGrid());
        MoistureMap moistureMap = new MoistureMap(dimX, dimY, gridSpacing, fileManager.getMoistureGrid());
        SunlightMap sunligntMap = new SunlightMap(dimX, dimY, gridSpacing, fileManager.getSunlightGrid());
        AbioticFactors abioticFactors = new AbioticFactors(moistureMap, temperatureMap, sunligntMap);
        Terrain terrain = new Terrain(dimX, dimY, gridSpacing, abioticFactors, fileManager.getElevationGrid());

        //vizualizer for the abiotic factors
        GridVisualizer.showGrid(fileManager.getMoistureGrid(), "Moisture Map");
        GridVisualizer.showGrid(fileManager.getTemperatureGrid(), "Temperature Map");
        GridVisualizer.showGrid(fileManager.getSunlightGrid(), "Sunlight Map");
        GridVisualizer.showGrid(fileManager.getElevationGrid(), "Elevation Map");

        moistureMap.testMoisture();
        temperatureMap.testTemperature();
        sunligntMap.testSunlight();
        terrain.testTerrain();
        // test completed. files read in successfully.

        // load in data for different species
        List<Species> speciesList = new ArrayList<>();
        SpeciesDictionary speciesDictionary = new SpeciesDictionary();
        Species boxwood = speciesDictionary.loadBoxwood();
        speciesList.add(boxwood);
        Species snowyMespilus = speciesDictionary.loadSnowyMespilus();
        speciesList.add(snowyMespilus);
        Species mountainPine = speciesDictionary.loadMountainPine();
        speciesList.add(mountainPine);
        Species silverFir = speciesDictionary.loadSilverFir();
        speciesList.add(silverFir);
        Species silverBirch = speciesDictionary.loadSilverBirch();
        speciesList.add(silverBirch);
        Species sissileOak = speciesDictionary.loadSissileOak();
        speciesList.add(sissileOak);
        Species europeanBeech = speciesDictionary.loadEuropeanBeech();
        speciesList.add(europeanBeech);


                // === TESTING PINK NOISE SAMPLER ===
        PinkNoiseSampler sampler = new PinkNoiseSampler(dimX, dimY, 2.0f, 42L); 
        // dimX/dimY from file, 2.0f = 2m min separation, 42L = random seed
        List<PointSample> samples = sampler.generateSamples(1000); // try 1000 canopy trees

        // Show results in Swing window
        SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Pink Noise Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PinkNoiseVisualizer(samples, dimX, dimY));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        });

        // add plants to pink noise :
        // run density function
        // run growth function

        //viabilityCalculator setup
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abioticFactors);
        double boxwoodViability;
        double snowyMespilusViability;
        double mountainPineViability;
        double silverFirViability;
        double silverBirchViability;
        double sissileOakViability;
        double europeanBeechViability;
        float sumViabilites;

        //setup species maps
        SpeciesMap boxwodMap = new SpeciesMap(boxwood);
        SpeciesMap snowyMespilusMap = new SpeciesMap(snowyMespilus);
        SpeciesMap mountainPineMap = new SpeciesMap(mountainPine);
        SpeciesMap silverFirMap = new SpeciesMap(silverFir);
        SpeciesMap silverBirchMap = new SpeciesMap(silverBirch);
        SpeciesMap sissileOakMap = new SpeciesMap(sissileOak);
        SpeciesMap europeanBeechMap = new SpeciesMap(europeanBeech);


        for (int i = 0; i < samples.size(); i++){
            int xPos = (int)samples.get(i).getX();
            int yPos = (int)samples.get(i).getY();
            // run viability function
            boxwoodViability = calc.viabililty(boxwood, xPos, yPos);
            boxwood.setViabilityAtPoint(boxwoodViability);
            snowyMespilusViability = calc.viabililty(snowyMespilus, xPos, yPos);
            snowyMespilus.setViabilityAtPoint(snowyMespilusViability);
            mountainPineViability = calc.viabililty(mountainPine, xPos, yPos);
            mountainPine.setViabilityAtPoint(mountainPineViability);
            silverFirViability = calc.viabililty(silverFir, xPos, yPos);
            silverFir.setViabilityAtPoint(silverFirViability);
            silverBirchViability = calc.viabililty(silverBirch, xPos, yPos);
            silverBirch.setViabilityAtPoint(silverBirchViability);
            sissileOakViability = calc.viabililty(sissileOak, xPos, yPos);
            sissileOak.setViabilityAtPoint(sissileOakViability);
            europeanBeechViability = calc.viabililty(europeanBeech, xPos, yPos);
            europeanBeech.setViabilityAtPoint(europeanBeechViability);

            //sum of all viabilites
            sumViabilites = (float)(boxwoodViability + snowyMespilusViability + mountainPineViability + silverBirchViability + silverFirViability + sissileOakViability + europeanBeechViability);
            //run roulette wheel
            RouletteWheelSelector rw = new RouletteWheelSelector(sumViabilites);
            String speciesSelected = rw.selectSpecies(speciesList);
            //need age, size etc, allometry etc...
            if (speciesSelected == "Boxwood"){
                boxwodMap.setPlantAt(new Plant(i, xPos, yPos, age, boxwood, size, true, i, false), xPos, yPos);
            
            }
        }
        // add plants to forest object
        // create forest object
        // overlay forest on terrain
        // export to 2D renderer

        
    }

    public void startSimulation() {
        // Method stub
    }
    
    public void pauseSimulation() {
        // Method stub
    }
    
    public void resetSimulation() {
        // Method stub
    }
    
    public void applyGlobalChanges() {
        // Method stub
    }
    
    public void save(String folderPath) {
        // Method stub
    }
    
    public void load(String folderPath) {
        // Method stub
    }
    
}
