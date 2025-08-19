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
        int gridSpacing = fileManager.getGridSpacing();

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
        Species europeanOak = loadEuropeanOak();
        Species europeanBeech = loadEuropeanBeech();
        
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
        System.out.println(boxwood.toString());

        return boxwood;
    }

    private static Species loadSnowyMespilus()
    {

        return snowyMespilus;
    }

    private static Species loadMountainPine()
    {

        return mountainPine;
    }

    private static Species loadSilverFir()
    {

        return silverFir;
    }

    private static Species loadSilverBirch()
    {

        return silverBirch;
    }

    private static Species loadSissileOak()
    {

        return sissileOak;
    }

    private static Species loadEuropeanOak()
    {

        return europeanOak;
    }

    private static Species loadEuropeanBeech()
    {

        return europeanBeech;
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
