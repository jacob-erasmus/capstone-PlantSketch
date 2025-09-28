package plantsketch.ui;

import plantsketch.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple forest visualization component that displays plants as colored circles on a black background.
 * This is a simpler version of ForestOnMapView - it shows only the plants without any environmental
 * background data. Think of it as a "plants-only" view that's faster to render and easier to see
 * the forest structure clearly.
 *
 * Key features:
 * - Black background for high contrast
 * - Plants shown as semi-transparent colored circles
 * - Zoom in/out functionality
 * - Each species gets its own color
 * - Plant size reflects canopy radius
 */
public class ForestView extends Region {
    // Core data and rendering
    private final Forest forest;                        // The ecosystem containing all plants and species
    private ViewTransform vt;                          // Handles coordinate conversion (meters ↔ pixels)
    private final Canvas canvas = new Canvas();        // Single canvas for drawing everything
    private final double minCirclePx = 2.0;           // Minimum plant size in pixels (ensures visibility)

    // Grid configuration
    boolean useDefaultCellSize;  // Whether to use adaptive sizing for small grids
    int dimX;                    // Number of grid cells in X direction
    int dimY;                    // Number of grid cells in Y direction
    float gridSpacing;           // Real-world distance between grid cells in meters

    // Performance optimization: cache parsed colors to avoid repeated string parsing
    private Map<String, Color> colorCache = new HashMap<>();


    /**
     * Creates a new forest-only visualization.
     *
     * @param forest The ecosystem to visualize
     * @param dimX Number of grid cells in X direction (determines world size)
     * @param dimY Number of grid cells in Y direction (determines world size)
     * @param gridSpacing Real-world distance between grid cells in meters
     */
    public ForestView(Forest forest, int dimX, int dimY, float gridSpacing) {
        this.forest = forest;
        this.dimX = dimX;
        this.dimY = dimY;
        this.gridSpacing = gridSpacing;

        // For small grids, use larger cell sizes to make them visible
        useDefaultCellSize = true;
        if (dimX < 256){
            useDefaultCellSize = false; // Use adaptive sizing for small grids
        }

        // Create coordinate transformation system (grid cells ↔ meters ↔ pixels)
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);

        // Set up the JavaFX scene graph
        updateRegionSize();           // Size this component to match the forest area
        canvas.setLayoutX(0);         // Position canvas at origin
        canvas.setLayoutY(0);
        getChildren().add(canvas);    // Add canvas to this container

        draw(); // Initial render
    }

    /**
     * Updates the size of this component and canvas to match the current view transform.
     * Called when zooming or resetting the view.
     */
    private void updateRegionSize(){
        // Set this JavaFX Region's size constraints
        setPrefSize(vt.widthPx, vt.heightPx);   // Preferred size
        setMinSize(vt.widthPx, vt.heightPx);    // Minimum size (prevents shrinking)
        setMaxSize(vt.widthPx, vt.heightPx);    // Maximum size (prevents growing)

        // Resize canvas to match
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);

        requestLayout(); // Tell JavaFX to re-layout this component
    }
    /**
     * Zooms in by 20%, making plants appear larger and showing more detail.
     * Capped at 3000 pixels to prevent excessive memory usage.
     */
    public void zoomIn(){
        // Calculate new size: 20% larger but not exceeding 3000px
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.min(currentSize * 1.2, 3000);

        // Create new coordinate transformation with larger pixel dimensions
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);

        // Update display
        updateRegionSize();
        draw(); // Redraw at new scale
    }
    /**
     * Zooms out by 20%, making plants appear smaller and showing less detail.
     * Capped at 256 pixels minimum to maintain visibility.
     */
    public void zoomOut(){
        // Calculate new size: 20% smaller but not below 256px
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);

        // Create new coordinate transformation with smaller pixel dimensions
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);

        // Update display
        updateRegionSize();
        draw(); // Redraw at new scale
    }
    /**
     * Resets the zoom level to the default size (1:1 pixel to grid cell ratio).
     */
    public void resetToDefault(){
        // Reset to original view transform (default sizing)
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);

        // Update display
        updateRegionSize();
        draw(); // Redraw at default scale
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
     * Draws the entire forest visualization.
     * This creates a clean, high-contrast view with plants as colored circles on a black background.
     * Much simpler than ForestOnMapView since there's no environmental background to render.
     */
    public void draw() {
        // Ensure canvas is properly sized
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);

        GraphicsContext g = canvas.getGraphicsContext2D();

        // Paint the background black for high contrast
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, vt.widthPx, vt.heightPx);

        // Draw all plants, grouped by species for efficiency
        for (SpeciesMap sm : forest.getSpeciesMapList()) {
            // Get or create cached color for this species
            // Each species gets its own color with 85% opacity for a softer look
            Color c = colorCache.computeIfAbsent(sm.getSpecies().getColour(),
                colorStr -> parseColour(colorStr).deriveColor(0, 1, 1, 0.85));
            g.setFill(c);

            // Draw all plants of this species
            for (Plant p : sm.getPlants()) {
                // Convert plant position from meters to pixel coordinates
                double xPx = vt.meterXtoPx(p.getX());
                double yPx = vt.meterYtoPx(p.getY());

                // Convert canopy radius to pixels (minimum size for visibility)
                double rPx = Math.max(minCirclePx, vt.metersToPx(p.getCanopyRadius()));

                // Draw plant as a circle (oval with equal width/height)
                g.fillOval(xPx - rPx, yPx - rPx, rPx * 2, rPx * 2);
            }
        }

        // Draw a subtle gray border around the entire forest area
        g.setStroke(Color.GRAY);
        g.strokeRect(0.5, 0.5, vt.widthPx - 1, vt.heightPx - 1);
    }

    /**
     * JavaFX layout method - ensures the canvas is positioned at (0,0)
     * relative to this container.
     */
    @Override protected void layoutChildren() {
        canvas.relocate(0, 0); // Position canvas at origin
    }
}
