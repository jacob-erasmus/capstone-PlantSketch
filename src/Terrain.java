
public class Terrain {
    int width;
    int height;
    int cellSize;
    float[][] elevationMap;
    float[][] slopeMap;
    //AbioticFactors abioticFactors;

    //Constructor
    public Terrain(int width, int height, int cellSize){
        this.width = width;
        this.height = height;
        this.cellSize = cellSize; 
    }

    //Get Elevation Method
    public float getElevation(int x, int y){
        return elevationMap[x][y];
    }

    //Get Slope Method
    public float getSlope(int x, int y){
        //Approximate partial derivatives
        float dzdx = (elevationMap[x+1][y] - elevationMap[x-1][y])/(2*cellSize);
        float dzdy = (elevationMap[x][y+1] + elevationMap[x][y-1])/(2*cellSize);
        //normal vector
        Vector n = new Vector(-dzdx, -dzdy, 1);
        n.normalise();
        //vertical vector
        Vector vertical = new Vector(0, 0, 1);
        //Calculate slope
        float slope = (float)Math.toDegrees(Math.acos(n.dot(vertical)));
        return slope;
    }
}