public class RouletteWheelSelector {
    private double[] selectionWeights;
    
    public RouletteWheelSelector() {
        this.selectionWeights = new double[0];
    }
    
    public RouletteWheelSelector(double[] weights) {
        this.selectionWeights = weights.clone();
    }
    
    public Object select(Object[] candidates, double[] weights) {
        // Method stub
        return null;
    }
    
    public double[] normalizeWeights(double[] weights) {
        // Method stub
        return new double[0];
    }
}