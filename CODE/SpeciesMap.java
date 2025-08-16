public class SpeciesMap {
    private Species[][] speciesData;
    private int width;
    private int height;
    
    public SpeciesMap() {
        this.width = 0;
        this.height = 0;
        this.speciesData = null;
    }
    
    public SpeciesMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.speciesData = new Species[height][width];
    }
    
    public Species getSpeciesAt(int x, int y) {
        // Method stub
        return null;
    }
    
    public void setSpeciesAt(int x, int y, Species species) {
        // Method stub
    }
}
