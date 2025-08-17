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


    public static void main(String[] args)
    {
        // option 1: upload all files
        // 2: upload .pdb file
        // read in files
        FileManager fileManager = new FileManager();
        fileManager.fileFinder();
    }
}
