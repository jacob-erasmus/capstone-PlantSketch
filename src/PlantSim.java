public class PlantSim {
    private Terrain terrain;
    private Forest forest;
    private Renderer2D renderer;
    private UIController uiController;
    private FileManager fileManager;
    private boolean isRunning;
    
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

        //TESTING SECTION OF MAIN METHOD

        //Boxwood Declaration
        ViabilityParameters boxwoodViabilityParameters = new ViabilitlyParameters(3.75, 4.25,
                            27.5, 12.5,
                            11.75, 23.35,
                            0, 80);
        
        GrowthParameters boxwoodGrowthParameters = new GrowthParameters(9, 9,
                         -5, 300);

        Species boxwood = new Species("Boxwood",
                   boxwoodViabilityParameters,
                   boxwoodGrowthParameters,
                   "Red",
                   0.42,
                   0.42,
                   0.70,
                   15,
                   "L");
        System.out.println(boxwood.toString());
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
