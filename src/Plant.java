public class Plant {

    // Attributes
    private int id;
    private float x;
    private float y;
    private int age;
    private Species species;
    private float size;        // e.g. canopy radius or height
    private boolean isAlive;
    private float vigour;      // growth health index

    // Constructor
    public Plant(int id, float x, float y, int age, Species species, float size, boolean isAlive, float vigour) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.age = age;
        this.species = species; // link to shared species data
        this.size = size;
        this.isAlive = isAlive;
        this.vigour = vigour;
    }

    // Getters
    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAge() {
        return age;
    }

    public Species getSpecies() {
        return species;
    }

    public float getSize() {
        return size;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public float getVigour() {
        return vigour;
    }

    // Setters
    public void setAge(int age) {
        this.age = age;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void setVigour(float vigour) {
        this.vigour = vigour;
    }

    @Override
    public String toString() {
        return "Plant {" +
                "\n  id=" + id +
                ",\n  x=" + x +
                ",\n  y=" + y +
                ",\n  age=" + age +
                ",\n  species=" + species.getName() +   // use Species association
                ",\n  size=" + size +
                ",\n  isAlive=" + isAlive +
                ",\n  vigour=" + vigour +
                "\n}";
    }
}
