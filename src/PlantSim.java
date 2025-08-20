import javax.swing.*;
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

        moistureMap.testMoisture();
        temperatureMap.testTemperature();
        sunligntMap.testSunlight();
        terrain.testTerrain();
        // test completed. files read in successfully.

        // load in data for different species
        SpeciesDictionary speciesDictionary = new SpeciesDictionary();
        Species boxwood = speciesDictionary.loadBoxwood();
        Species snowyMespilus = speciesDictionary.loadSnowyMespilus();
        Species mountainPine = speciesDictionary.loadMountainPine();
        Species silverFir = speciesDictionary.loadSilverFir();
        Species silverBirch = speciesDictionary.loadSilverBirch();
        Species sissileOak = speciesDictionary.loadSissileOak();
        Species europeanBeech = speciesDictionary.loadEuropeanBeech();

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
