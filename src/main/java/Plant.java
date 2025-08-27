public class Plant {

    // ============================
    // Attributes
    // ============================

    private int id;
    private float x;
    private float y;
    private float currentAge;
    private Species species;
    private float canopyRadius;
    private float height;
    private boolean isAlive;
    private float vigour;
    private boolean allometryIsOpen; // open vs closed growth mode

    // ============================
    // Constructor
    // ============================

    //
    // I think get rid of the coordinates and just use the point sample coordinates
    //
    public Plant(int id, float x, float y, float currentAge, Species species,
                 float canopyRadius, float height, boolean isAlive, float vigour, boolean allometryIsOpen) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.currentAge = currentAge;
        this.species = species; // link to shared species data
        this.canopyRadius = canopyRadius;
        this.height = height;
        this.isAlive = isAlive;
        this.vigour = vigour;
        this.allometryIsOpen = allometryIsOpen;
    }

    // ============================
    // Plant getters
    // ============================

    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAge() {
        return currentAge;
    }

    public Species getSpecies() {
        return species;
    }

    public float getCanopyRadius() {
        return canopyRadius;
    }

    public float getHeight(){
        return height;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public float getVigour() {
        return vigour;
    }

    public boolean isAllometryOpen() {
        return allometryIsOpen;
    }

    // ============================
    // Plant setters
    // ============================

    public void setAge(int currentAge) {
        this.currentAge = currentAge;
    }

    public void setCanopyRadius(float canopyRadius) {
        this.canopyRadius = canopyRadius;
    }

    public void setHeight(float height){
        this.height = height;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void setVigour(float vigour) {
        this.vigour = vigour;
    }

    public void setAllometryIsOpen(boolean open) {
        this.allometryIsOpen = open;
    }

    // ============================
    // Delegated Species accessors
    // ============================

    public String getSpeciesName() {
        return species.getName();
    }

    public float getLifeSpan() {
        return species.getLifeSpan();
    }

    public float getSunlightC() {
        return species.getSunlightC();
    }

    public float getSunlightR() {
        return species.getSunlightR();
    }

    public float getMoistureC() {
        return species.getMoistureC();
    }

    public float getMoistureR() {
        return species.getMoistureR();
    }

    public float getTemperatureC() {
        return species.getTemperatureC();
    }

    public float getTemperatureR() {
        return species.getTemperatureR();
    }

    public float getSlopeC() {
        return species.getSlopeC();
    }

    public float getSlopeR() {
        return species.getSlopeR();
    }

    public String getColour() {
        return species.getColour();
    }

    public float getMaxHeightOpen() {
        return species.getMaxHeightOpen();
    }

    public float getMaxHeightClosed() {
        return species.getMaxHeightClosed();
    }

    public float getQ() {
        return species.getQ();
    }

    public float getRadiusMultiplierOpen() {
        return species.getRadiusMultiplierOpen();
    }

    public float getRadiusMultiplierClosed() {
        return species.getRadiusMultiplierClosed();
    }

    public float getLeafTransparency() {
        return species.getLeafTransparency();
    }

    public float getMoistureAbsorbtion() {
        return species.getMoistureAbsorbtion();
    }

    public String getGrowthPeriod() {
        return species.getGrowthPeriod();
    }

    // ============================
    // Utility
    // ============================

    @Override
    public String toString() {
        return "Plant {" +
                "\n  id=" + id +
                ",\n  x=" + x +
                ",\n  y=" + y +
                ",\n  currentAge=" + currentAge +
                ",\n  species=" + getSpeciesName() +
                ",\n  canopyRadius=" + canopyRadius +
                ",\n  height=" + height +
                ",\n  isAlive=" + isAlive +
                ",\n  vigour=" + vigour +
                ",\n  allometryIsOpen=" + allometryIsOpen +
                "\n}";
    }
}
