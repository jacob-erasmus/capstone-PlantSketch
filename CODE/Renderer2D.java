public class Renderer2D {
    private Object canvas;
    private Brush brush;
    private Object viewport;
    
    public Renderer2D() {
        this.canvas = null;
        this.brush = null;
        this.viewport = null;
    }
    
    public Renderer2D(Object canvas) {
        this.canvas = canvas;
        this.brush = new Brush();
        this.viewport = null;
    }
    
    public void renderTerrain(Terrain terrain) {
        // Method stub
    }
    
    public void renderPlants(List<Plant> plants) {
        // Method stub
    }
    
    public void renderSpeciesMap(SpeciesMap speciesMap) {
        // Method stub
    }
    
    public void clearCanvas() {
        // Method stub
    }
}
