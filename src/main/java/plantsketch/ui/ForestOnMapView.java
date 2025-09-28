package plantsketch.ui;

import plantsketch.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import plantsketch.util.PerformanceTimer;

public class ForestOnMapView extends Region {
    // Core data structures
    private Forest forest;              // The ecosystem containing all plants and species
    private float[][] map;              // Environmental data grid [dimX][dimY] (e.g., elevation)
    private ViewTransform vt;           // Handles coordinate conversion between grid cells, meters, and pixels
    private Canvas canvas = new Canvas(); // Legacy canvas (unused but kept for compatibility)
    private float gridSpacing;          // Real-world distance between grid cells in meters

    // Display settings
    private boolean useDefaultCellSize; // Whether to use adaptive cell sizing for small grids

    // Species selection for interactive editing
    private Set<String> selectedSpecies;                    // Currently selected species names
    private Supplier<Set<String>> selectedSpeciesSupplier; // Function to get selected species from UI

    // Rendering layers - separate canvases for better performance
    private final Canvas mapCanvas = new Canvas();    // Background layer: environmental map
    private final Canvas forestCanvas = new Canvas(); // Foreground layer: plants and organisms

    // Performance optimizations
    private Map<String, Color> colorCache = new HashMap<>(); // Cache parsed colors to avoid repeated parsing
    private float mapMin, mapMax, mapRange;                  // Pre-calculated map statistics for faster rendering

    // Performance optimization: Pre-computed grayscale colors to avoid creating new Color objects
    // during every map cell render (can be thousands of cells per frame)
    private static final Color[] GRAYSCALE = new Color[256];
    static {
        for (int i = 0; i < 256; i++) {
            GRAYSCALE[i] = Color.rgb(i, i, i); // Create all shades from black (0) to white (255)
        }
    }

    /**
     * Creates a new forest visualization on top of an environmental map.
     *
     * @param forest The ecosystem to visualize (contains plants and species data)
     * @param map 2D array of environmental values (e.g., elevation, moisture) [x][y]
     * @param gridSpacing Real-world distance between map grid cells in meters
     */
    public ForestOnMapView(Forest forest, float[][] map, float gridSpacing) {
        this.forest = forest;
        this.map = map;
        int dimX = map.length;        // Number of grid cells in X direction
        int dimY = map[0].length;     // Number of grid cells in Y direction

        // For small grids, use larger cell sizes to make them visible
        useDefaultCellSize = true;
        if (dimX < 256){
            useDefaultCellSize = false; // Use adaptive sizing for small grids
        }

        this.gridSpacing = gridSpacing;
        // Create coordinate transformation system (grid cells ↔ meters ↔ pixels)
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);

        // Pre-calculate map statistics for efficient grayscale rendering
        // This avoids recalculating min/max values for every frame
        mapMin = Float.MAX_VALUE;
        mapMax = -Float.MAX_VALUE;
        for (int x = 0; x < dimX; x++) {
            for (int y = 0; y < dimY; y++) {
                float v = map[x][y];
                if (v < mapMin) mapMin = v;
                if (v > mapMax) mapMax = v;
            }
        }
        mapRange = Math.max(1e-9f, mapMax - mapMin); // Prevent division by zero

        // Set up the JavaFX scene graph
        updateRegionSize();           // Size this component to match the map
        mapCanvas.setLayoutX(0);      // Position background layer
        mapCanvas.setLayoutY(0);
        forestCanvas.setLayoutX(0);   // Position foreground layer
        forestCanvas.setLayoutY(0);
        getChildren().addAll(mapCanvas, forestCanvas); // Add both layers to this container

        // Initial render
        drawMap();    // Draw the environmental background
        drawForest(); // Draw the plants on top
    }

    /**
     * Updates the size of this component and all canvases to match the current view transform.
     * Called when zooming or resetting the view.
     */
    private void updateRegionSize(){
        // Set this JavaFX Region's size constraints
        setPrefSize(vt.widthPx, vt.heightPx);   // Preferred size
        setMinSize(vt.widthPx, vt.heightPx);    // Minimum size (prevents shrinking)
        setMaxSize(vt.widthPx, vt.heightPx);    // Maximum size (prevents growing)

        // Resize both canvas layers to match
        mapCanvas.setWidth(vt.widthPx);
        mapCanvas.setHeight(vt.heightPx);
        forestCanvas.setWidth(vt.widthPx);
        forestCanvas.setHeight(vt.heightPx);

        requestLayout(); // Tell JavaFX to re-layout this component
    }
    /**
     * Zooms in by 20%, making the map appear larger and showing more detail.
     * Capped at 3000 pixels to prevent excessive memory usage.
     */
    public void zoomIn(){
        int dimX = map.length;
        int dimY = map[0].length;

        // Calculate new size: 20% larger but not exceeding 3000px
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.min(currentSize * 1.2, 3000);

        // Create new coordinate transformation with larger pixel dimensions
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);

        // Update display
        updateRegionSize();
        drawMap();    // Redraw background at new scale
        drawForest(); // Redraw plants at new scale
    }
    /**
     * Zooms out by 20%, making the map appear smaller and showing less detail.
     * Capped at 256 pixels minimum to maintain visibility.
     */
    public void zoomOut(){
        int dimX = map.length;
        int dimY = map[0].length;

        // Calculate new size: 20% smaller but not below 256px
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);

        // Create new coordinate transformation with smaller pixel dimensions
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);

        // Update display
        updateRegionSize();
        drawMap();    // Redraw background at new scale
        drawForest(); // Redraw plants at new scale
    }
    /**
     * Resets the zoom level to the default size (1:1 pixel to grid cell ratio).
     */
    public void resetToDefault(){
        int dimX = map.length;
        int dimY = map[0].length;

        // Reset to original view transform (default sizing)
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);

        // Update display
        updateRegionSize();
        drawMap();    // Redraw at default scale
        drawForest(); // Redraw at default scale
    }

    /**
     * Safely converts a color string (hex code or named color) to a JavaFX Color object.
     * Falls back to lime green if the string can't be parsed.
     */
    private static Color parseColour(String hexOrName) {
        try {
            return Color.web(hexOrName); // Parse hex codes like "#FF0000" or names like "red"
        } catch (Exception e) {
            return Color.LIMEGREEN; // Safe fallback color
        }
    }

    /**
     * Redraws both the map background and forest foreground.
     * Called when the forest data changes or display settings are updated.
     */
    public void draw() {
        drawMap();    // Redraw environmental background
        drawForest(); // Redraw plants and organisms
    }

    /**
     * Draws the environmental map as a grayscale background.
     * Each grid cell is colored based on its environmental value (darker = lower, lighter = higher).
     */
    private void drawMap(){
        // Ensure canvas is properly sized
        mapCanvas.setWidth(vt.widthPx);
        mapCanvas.setHeight(vt.heightPx);

        GraphicsContext g = mapCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight()); // Clear previous drawing

        // Draw each grid cell as a grayscale rectangle
        double cs = vt.cellPx; // Size of each cell in pixels
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                // Normalize the environmental value to 0-1 range
                double t = (map[x][y] - mapMin) / mapRange;

                // Convert to grayscale intensity (0-255)
                int gray = Math.max(0, Math.min(255, (int) Math.round(t * 255)));

                // Use pre-computed color to avoid object creation
                g.setFill(GRAYSCALE[gray]);

                // Convert grid coordinates to pixel coordinates
                double px = vt.cellXtoPx(x);
                double py = vt.cellYtoPx(y);

                // Draw the cell as a filled rectangle
                g.fillRect(px, py, cs, cs);
            }
        }

        // Draw border around the entire map
        g.setStroke(Color.BLACK);
        g.strokeRect(0.5, 0.5, vt.widthPx -1, vt.heightPx -1);
    }

    /**
     * Draws all plants in the forest as colored circles on the foreground layer.
     * Each species gets its own color, and plant size reflects canopy radius.
     */
    private void drawForest(){
        // Ensure canvas is properly sized
        forestCanvas.setWidth(vt.widthPx);
        forestCanvas.setHeight(vt.heightPx);

        GraphicsContext g = forestCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, forestCanvas.getWidth(), forestCanvas.getHeight()); // Clear previous drawing

        // Draw plants grouped by species (for efficiency)
        for (SpeciesMap sm : forest.getSpeciesMapList()) {
            // Get or create cached color for this species
            Color c = colorCache.computeIfAbsent(sm.getSpecies().getColour(),
                colorStr -> parseColour(colorStr).deriveColor(0, 1, 1, 0.85)); // 85% opacity
            g.setFill(c);

            // Draw all plants of this species
            for (Plant p : sm.getPlants()) {
                // Convert plant position from meters to pixel coordinates
                double xPx = vt.meterXtoPx(p.getX());
                double yPx = vt.meterYtoPx(p.getY());

                // Convert canopy radius to pixels (minimum 2px for visibility)
                double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));

                // Draw plant as a circle (oval with equal width/height)
                g.fillOval(xPx - rPx, yPx - rPx, rPx * 2, rPx * 2);
            }
        }

        // Draw border around the entire forest area
        g.setStroke(Color.BLACK);
        g.strokeRect(0.5, 0.5, vt.widthPx -1, vt.heightPx -1);
    }
    /**
     * Updates the selected species set by calling the supplier function.
     * This allows the UI to control which species are affected by brush tools.
     */
    private void setSelectedSpecies(){
        this.selectedSpecies = (selectedSpeciesSupplier != null) ?
            selectedSpeciesSupplier.get() : Collections.emptySet();
    }

    /**
     * Sets the function that provides the currently selected species names.
     * This creates a connection between the UI controls and the brush tools.
     */
    public void setSelectedSpeciesSupplier(Supplier<Set<String>> supplier){
        this.selectedSpeciesSupplier = supplier;
    }
    /**
     * Enables interactive plant removal mode. Click and drag to remove plants within the brush area.
     * Only affects plants of the currently selected species.
     */
    public void enableBrushRemovalMode(Supplier<Double> brushSizeSupplier){
        // Set up mouse event handlers for removal brush
        forestCanvas.setOnMousePressed(e ->
            applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()/3));
        forestCanvas.setOnMouseDragged(e ->
            applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()/3));
    }
    /**
     * Enables interactive cohort age modification mode. 
     * Modifies the underlying environmental age map.
     */
    public void enableBrushAgeMode(Supplier<Double> brushSizeSupplier, Supplier<Double> ageSupplier, SimulationEngine simulationEngine){
        // Set up mouse event handlers for age modification brush
        forestCanvas.setOnMousePressed(e ->
            applyBrushAge(e.getX(), e.getY(), brushSizeSupplier.get()/3, ageSupplier.get(), simulationEngine));
        forestCanvas.setOnMouseDragged(e ->
            applyBrushAge(e.getX(), e.getY(), brushSizeSupplier.get()/3, ageSupplier.get(), simulationEngine));
    }
    /**
     * Disables all brush interaction modes by removing mouse event handlers.
     * Returns the view to read-only visualization mode.
     */
    public void disableBrushMode(){
        forestCanvas.setOnMousePressed(null);
        forestCanvas.setOnMouseDragged(null);
        forestCanvas.setOnMouseReleased(null);
    }

    /**
     * Removes plants within the brush area. Only affects plants of selected species.
     * Uses collision detection between the circular brush and plant canopies.
     */
    private void applyBrushRemoval(double brushX, double brushY, double brushSize){
        long brushRemovalStartTime = System.nanoTime(); // Performance timing
        double brushRadiusPx = brushSizeToPixels(brushSize);

        setSelectedSpecies(); // Get current species selection from UI
        List<Plant> toRemove = new ArrayList<>();

        // Check each plant for collision with brush
        for(Plant p : forest.getAllPlants()){
            // Skip plants not in selected species
            if (!selectedSpecies.contains(p.getSpeciesName())){
                continue;
            }

            // Convert plant position to pixel coordinates
            double xPx = vt.meterXtoPx(p.getX());
            double yPx = vt.meterYtoPx(p.getY());
            double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));

            // Calculate distance between brush center and plant center
            double dx = brushX - xPx;
            double dy = brushY - yPx;
            double distSquared = dx*dx + dy*dy;
            double thresholdSquared = (brushRadiusPx + rPx) * (brushRadiusPx + rPx);
            //if plant canopy contacts brush radius
            if(distSquared <= thresholdSquared){
                toRemove.add(p);
            }
        }

        // Remove all marked plants and update display
        if (!toRemove.isEmpty()){
            for(Plant p : toRemove){
                forest.removePlant(p);
            }
            drawForest(); // Redraw to show changes
            System.out.println("Apply Brush Removal Elapsed Time: " + (System.nanoTime() - brushRemovalStartTime) +
                " (nanoseconds). Removed " + toRemove.size() + " plants");
        }
    }

    /**
     * Modifies plant ages and environmental age data within the brush area.
     * This is a two-step process:
     * 1. Update environmental age map for all grid cells in brush area
     * 2. Update individual plant ages for plants touching the brush
     */
    private void applyBrushAge(double brushX, double brushY, double brushSize, double ageFactor, SimulationEngine simulationEngine){
        long brushStartTime = System.nanoTime();
        List<Plant> toChange = new ArrayList<>();
        double brushRadiusPx = brushSizeToPixels(brushSize);
        double brushXMeters = vt.pxToMeterX(brushX);
        double brushYMeters = vt.pxToMeterY(brushY);
        double brushRadiusMeters = vt.pxToMeterX(brushRadiusPx);

        // Calculate bounding box in grid cell indices for efficiency
        // Only check cells that could possibly be in the circular brush area
        int minCellX = Math.max(0, (int)((brushXMeters - brushRadiusMeters) / gridSpacing));
        int maxCellX = Math.min(map.length - 1, (int)((brushXMeters + brushRadiusMeters) / gridSpacing));
        int minCellY = Math.max(0, (int)((brushYMeters - brushRadiusMeters) / gridSpacing));
        int maxCellY = Math.min(map[0].length - 1, (int)((brushYMeters + brushRadiusMeters) / gridSpacing));

        // Step 1: Update environmental age map for affected grid cells
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cy = minCellY; cy <= maxCellY; cy++) {
                // Find center of this grid cell in meters
                double cellXMeters = cx * gridSpacing + gridSpacing / 2.0;
                double cellYMeters = cy * gridSpacing + gridSpacing / 2.0;

                // Calculate distance from brush center to cell center
                double dx = brushXMeters - cellXMeters;
                double dy = brushYMeters - cellYMeters;
                double distMSquared = dx*dx + dy*dy;
                double brushRadiusMetersSquared = brushRadiusMeters * brushRadiusMeters;

                // Apply only if inside circular brush
                if (distMSquared <= brushRadiusMetersSquared) {
                    simulationEngine.adjustAge(cx, cy, (float)ageFactor);
                }
                PerformanceTimer.end("map_adjust");
            }
        }

        // Step 2: Find all plants that overlap with the brush area
        for(Plant p : forest.getAllPlants()){
            // Convert plant position to pixel coordinates
            double xPx = vt.meterXtoPx(p.getX());
            double yPx = vt.meterYtoPx(p.getY());
            double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));

            // Calculate distance between brush center and plant center
            double dx = brushX - xPx;
            double dy = brushY - yPx;
            double distSquared = dx*dx + dy*dy;
            double thresholdSquared = (brushRadiusPx + rPx) * (brushRadiusPx + rPx);
            //if plant canopy contacts brush radius
            if(distSquared <= thresholdSquared){
                toChange.add(p);
            }
        }

        // Apply age changes to all affected plants
        if (!toChange.isEmpty()){
            for(Plant p : toChange){
                //int xCell = (int) vt.meterXToCellX(p.getX());
                int xCell = (int) (p.getX() / gridSpacing);
                int yCell = (int) (p.getY() / gridSpacing);
                //int yCell = (int) vt.meterXToCellX(p.getY());
                simulationEngine.changePlantAge(xCell, yCell, (float)ageFactor, p);
            }
            drawForest();
            //System.out.println("Brush Elapsed Time: " + (System.nanoTime() - brushStartTime) + " (nanoseconds). Changed ages of " + toChange.size() + " plants");
        } 
    }
    /**
     * Converts brush size from grid cell units to pixel units.
     * This allows brush sizes to scale properly with zoom level.
     */
    private double brushSizeToPixels(double size){
        return size * vt.cellPx; // size in cells × pixels per cell = pixels
    }

    /**
     * JavaFX layout method - ensures both canvas layers are positioned at (0,0)
     * relative to this container.
     */
    @Override protected void layoutChildren() {
        mapCanvas.relocate(0, 0);    // Background layer at origin
        forestCanvas.relocate(0, 0); // Foreground layer at origin
    }
}
