public class Brush {
    private float radius;
    private Species selectedSpecies;
    private BrushType brushType;
    
    public Brush() {
        this.radius = 5.0f;
        this.selectedSpecies = null;
        this.brushType = BrushType.ADD;
    }
    
    public Brush(float radius) {
        this.radius = radius;
        this.selectedSpecies = null;
        this.brushType = BrushType.ADD;
    }
    
    public void applyBrush(int x, int y, Forest forest) {
        // Method stub
    }
    
    public void setRadius(float radius) {
        // Method stub
    }
    
    public void setSelectedSpecies(Species species) {
        // Method stub
    }
}
