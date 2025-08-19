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

        // load in data for different species
        Species boxwood = loadBoxwood();
        Species snowyMespilus = loadSnowyMespilus();
        Species mountainPine = loadMountainPine();
        Species silverFir = loadSilverFir();
        Species silverBirch = loadSilverBirch();
        Species sissileOak = loadSissileOak();
        Species europeanBeech = loadEuropeanBeech();

                // === TESTING PINK NOISE SAMPLER ===
        PinkNoiseSampler sampler = new PinkNoiseSampler(dimX, dimY, 2.0f, 42L); 
        // dimX/dimY from file, 2.0f = 2m min separation, 42L = random seed
        List<PointSample> samples = sampler.generateSamples(100); // try 100 canopy trees

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

    private static Species loadBoxwood()
    {
         ViabilityParameters boxwoodViabilityParameters = new ViabilityParameters(
        3.75f, 4.25f, 27.5f, 12.5f, 11.75f, 23.35f, 0f, 80f);
        GrowthParameters boxwoodGrowthParameters = new GrowthParameters(9f, 9f,
                         -5f, 300f);
        Species boxwood = new Species("Boxwood",
            boxwoodViabilityParameters,
            boxwoodGrowthParameters,
            "Red",
            0.42f,
            0.42f,
            0.70f,
            15f,
            "L"
        );
        // System.out.println(boxwood.toString());

        return boxwood;
    }

    private static Species loadSnowyMespilus()
    {
        ViabilityParameters snowyMespilusViabilityParameters = new ViabilityParameters(
        7f, 5f, 31f, 9f, 19.25f, 15.75f, 0f, 80f);
        GrowthParameters snowyMespilusGrowthParameters = new GrowthParameters(6f, 6f,
                         -4f, 50f);
        Species snowyMespilus = new Species("Snowy Mespilus",
            snowyMespilusViabilityParameters,
            snowyMespilusGrowthParameters,
            "Blue",
            0.41f,
            0.17f,
            0.52f,
            22f,
            "S"
        );
        //System.out.println(snowyMespilus.toString());
        return snowyMespilus;
    }

    private static Species loadMountainPine()
    {
        ViabilityParameters mountainPineViabilityParameters = new ViabilityParameters(
        7f, 5f, 21.5f, 18.5f, 11.75f, 23.25f, 0f, 80f);
        GrowthParameters mountainPineGrowthParameters = new GrowthParameters(15f, 20f,
                         -4f, 400f);
        Species mountainPine = new Species("Mountain Pine",
            mountainPineViabilityParameters,
            mountainPineGrowthParameters,
            "Green",
            0.16f,
            0.14f,
            0.43f,
            3f,
            "L"
        );
        //System.out.println(mountainPine.toString());
        return mountainPine;
    }

    private static Species loadSilverFir()
    {
        ViabilityParameters silverFirViabilityParameters = new ViabilityParameters(
        5f, 3f, 31f, 9f, 11.75f, 23.25f, 0f, 80f);
        GrowthParameters silverFirGrowthParameters = new GrowthParameters(40f, 50f,
                         -6f, 550f);
        Species silverFir = new Species("Silver Fir",
            silverFirViabilityParameters,
            silverFirGrowthParameters,
            "Purple",
            0.12f,
            0.07f,
            0.47f,
            22f,
            "L"
        );
        //System.out.println(silverFir.toString());
        return silverFir;
    }

    private static Species loadSilverBirch()
    {
        ViabilityParameters silverBirchViabilityParameters = new ViabilityParameters(
        8.25f, 3.75f, 27.5f, 12.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters silverBirchGrowthParameters = new GrowthParameters(18f, 25f,
                         -4f, 120f);
        Species silverBirch = new Species("Silver Birch",
            silverBirchViabilityParameters,
            silverBirchGrowthParameters,
            "Pink",
            0.2f,
            0.1f,
            0.30f,
            15f,
            "S"
        );
        //System.out.println(silverBirch.toString());
        return silverBirch;
    }

    private static Species loadSissileOak()
    {
        ViabilityParameters sissileOakViabilityParameters = new ViabilityParameters(
        5f, 3f, 37.5f, 22.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters sissileOakGrowthParameters = new GrowthParameters(30f, 45f,
                         -7f, 600f);
        Species sissileOak = new Species("Sissile Oak",
            sissileOakViabilityParameters,
            sissileOakGrowthParameters,
            "Yellow",
            0.38f,
            0.21f,
            0.35f,
            15f,
            "S"
        );
        //System.out.println(sissileOak.toString());
        return sissileOak;
    }

    private static Species loadEuropeanBeech()
    {
        ViabilityParameters europeanBeechViabilityParameters = new ViabilityParameters(
        5.75f, 6.25f, 37.5f, 22.5f, 19.25f, 15.75f, 0f, 70f);
        GrowthParameters europeanBeechGrowthParameters = new GrowthParameters(35f, 50f,
                         -4f, 400f);
        Species europeanBeech = new Species("European Beech",
            europeanBeechViabilityParameters,
            europeanBeechGrowthParameters,
            "Brown",
            0.3f,
            0.13f,
            0.37f,
            15f,
            "S"
        );
        //System.out.println(europeanBeech.toString());
        return europeanBeech;
    }
    
}
