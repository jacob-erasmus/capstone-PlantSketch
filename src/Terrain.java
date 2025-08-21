
public class Terrain {
    int width;
    int height;
    float gridSpacing;
    float[][] elevationMap;
    float[][] slopeMap;
    AbioticFactors abioticFactors;
    TemperatureMap temperatureMap;
    MoistureMap moistureMap;
    SunlightMap sunlightMap;

    //Constructor
    public Terrain(int width, int height, float gridSpacing, AbioticFactors abioticFactors, float[][] grid){
        this.width = width;
        this.height = height;
        this.gridSpacing = gridSpacing; 
        this.elevationMap = grid;
        this.slopeMap = new float[width][height];
        this.abioticFactors = abioticFactors;
    }

    //Get Elevation Method
    public float getElevation(int x, int y){
        return elevationMap[x][y];
    }

    //Get Slope Method
    public float getSlope(int x, int y){
        // Boundary checking - use forward/backward differences for edges
        int maxX = width - 1;
        int maxY = height - 1;
        
        if (x < 0 || x > maxX || y < 0 || y > maxY) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        
        //Approximate partial derivatives using appropriate differences
        float dzdx, dzdy;
        
        // Handle x-direction derivative
        if (x == 0) {
            // Forward difference at left boundary
            dzdx = (elevationMap[x+1][y] - elevationMap[x][y]) / gridSpacing;
        } else if (x == maxX) {
            // Backward difference at right boundary
            dzdx = (elevationMap[x][y] - elevationMap[x-1][y]) / gridSpacing;
        } else {
            // Central difference for interior points
            dzdx = (elevationMap[x+1][y] - elevationMap[x-1][y]) / (2*gridSpacing);
        }
        
        // Handle y-direction derivative
        if (y == 0) {
            // Forward difference at bottom boundary
            dzdy = (elevationMap[x][y+1] - elevationMap[x][y]) / gridSpacing;
        } else if (y == maxY) {
            // Backward difference at top boundary
            dzdy = (elevationMap[x][y] - elevationMap[x][y-1]) / gridSpacing;
        } else {
            // Central difference for interior points
            dzdy = (elevationMap[x][y+1] - elevationMap[x][y-1]) / (2*gridSpacing);
        }
        //normal vector
        Vector n = new Vector(-dzdx, -dzdy, 1);
        n.normalise();
        //vertical vector
        Vector vertical = new Vector(0, 0, 1);
        double dotProduct = n.dot(vertical);
        float slope = (float)Math.toDegrees(Math.acos(dotProduct));
        return slope;
    }

    //Set Abiotic Factors
    public void setAbioticFactors(){
        temperatureMap = abioticFactors.getTemperatureMap();
        sunlightMap = abioticFactors.getSunlightMap();
        moistureMap = abioticFactors.getMoistureMap();
    }

    public void testTerrain()
    {
        for(int i = 0; i < 13; i++) {
            System.out.println("Terrain at (0, "+i+"): " + getElevation(0, i));
        }
    }
}