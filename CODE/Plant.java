public class Plant {
    private int x;
    private int y;
    private Species species;
    private int age;
    private double height;
    private double health;
    private String growthStage;
    
    public Plant() {
        this.x = 0;
        this.y = 0;
        this.species = null;
        this.age = 0;
        this.height = 0.0;
        this.health = 100.0;
        this.growthStage = "seedling";
    }
    
    public Plant(int x, int y, Species species) {
        this.x = x;
        this.y = y;
        this.species = species;
        this.age = 0;
        this.height = 0.0;
        this.health = 100.0;
        this.growthStage = "seedling";
    }
    
    public void grow() {
        // Method stub
    }
    
    public int getSeedProduction() {
        // Method stub
        return 0;
    }
    
    public void die() {
        // Method stub
    }
}
