package plantsketch.ui;

import plantsketch.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class ForestOnMapView extends Region {
    private Forest forest;
    private float[][] map; // [dimX][dimY]
    private ViewTransform vt;
    private Canvas canvas = new Canvas();
    private float gridSpacing;
    private boolean useDefaultCellSize;
    private Set<String> selectedSpecies;
    private Supplier<Set<String>> selectedSpeciesSupplier;
    private final Canvas mapCanvas = new Canvas();
    private final Canvas forestCanvas = new Canvas();
    private Map<String, Color> colorCache = new HashMap<>();
    private float mapMin, mapMax, mapRange;

    // Pre-computed grayscale colors to avoid creating new Color objects
    private static final Color[] GRAYSCALE = new Color[256];
    static {
        for (int i = 0; i < 256; i++) {
            GRAYSCALE[i] = Color.rgb(i, i, i);
        }
    }

    public ForestOnMapView(Forest forest, float[][] map, float gridSpacing) {
        this.forest = forest;
        this.map = map;
        int dimX = map.length;
        int dimY = map[0].length;
        useDefaultCellSize = true;
        if (dimX < 256){
            useDefaultCellSize = false;
        }
        this.gridSpacing = gridSpacing;
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);

        // Calculate min/max once in constructor instead of every draw()
        mapMin = Float.MAX_VALUE;
        mapMax = -Float.MAX_VALUE;
        for (int x = 0; x < dimX; x++) {
            for (int y = 0; y < dimY; y++) {
                float v = map[x][y];
                if (v < mapMin) mapMin = v;
                if (v > mapMax) mapMax = v;
            }
        }
        mapRange = Math.max(1e-9f, mapMax - mapMin);

        //renderzoom
        updateRegionSize();
        mapCanvas.setLayoutX(0);
        mapCanvas.setLayoutY(0);
        forestCanvas.setLayoutX(0);
        forestCanvas.setLayoutY(0);
        getChildren().addAll(mapCanvas, forestCanvas);
        drawMap();
        drawForest();
    }

    private void updateRegionSize(){
        setPrefSize(vt.widthPx, vt.heightPx);
        setMinSize(vt.widthPx, vt.heightPx);
        setMaxSize(vt.widthPx, vt.heightPx);
        mapCanvas.setWidth(vt.widthPx);
        mapCanvas.setHeight(vt.heightPx);
        forestCanvas.setWidth(vt.widthPx);
        forestCanvas.setHeight(vt.heightPx);
        requestLayout();
    }
    public void zoomIn(){
        int dimX = map.length;
        int dimY = map[0].length;
        //increase render by 20% (capped at 3000)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.min(currentSize * 1.2, 3000);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        drawMap();
        drawForest();
    }
    public void zoomOut(){
        int dimX = map.length;
        int dimY = map[0].length;
        //decrease render by 20% (capped at 256)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        drawMap();
        drawForest();
    }
    public void resetToDefault(){
        int dimX = map.length;
        int dimY = map[0].length;
        //reset
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);
        updateRegionSize();
        drawMap();
        drawForest();
    }

    private static Color parseColour(String hexOrName) {
        try { return Color.web(hexOrName); } catch (Exception e) { return Color.LIMEGREEN; }
    }

    public void draw() {
        drawMap();
        drawForest();
        
    }

    private void drawMap(){
        mapCanvas.setWidth(vt.widthPx);
        mapCanvas.setHeight(vt.heightPx);
        GraphicsContext g = mapCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        // 1) draw elevation as grayscale (cells) - using cached min/max and pre-computed colors
        double cs = vt.cellPx;
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                double t = (map[x][y] - mapMin) / mapRange;
                int gray = Math.max(0, Math.min(255, (int) Math.round(t * 255)));
                g.setFill(GRAYSCALE[gray]);

                double px = vt.cellXtoPx(x);
                double py = vt.cellYtoPx(y);
                g.fillRect(py, px, cs, cs);
            }
        }
        g.setStroke(Color.BLACK);
        g.strokeRect(0.5, 0.5, vt.widthPx -1, vt.heightPx -1);
    }

    private void drawForest(){
        forestCanvas.setWidth(vt.widthPx);
        forestCanvas.setHeight(vt.heightPx);
        GraphicsContext g = forestCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, forestCanvas.getWidth(), forestCanvas.getHeight());
        // 2) draw plants on top (meters)
        for (SpeciesMap sm : forest.getSpeciesMapList()) {
                Color c = colorCache.computeIfAbsent(sm.getSpecies().getColour(),
                    colorStr -> parseColour(colorStr).deriveColor(0, 1, 1, 0.85));
                g.setFill(c);
                for (Plant p : sm.getPlants()) {
                    double xPx = vt.meterXtoPx(p.getX());
                    double yPx = vt.meterYtoPx(p.getY());
                    double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));
                    g.fillOval(yPx - rPx, xPx - rPx, rPx * 2, rPx * 2);
                    // swappiing them to plaster fix mirror image thing:    g.fillOval(xPx - rPx, yPx - rPx, rPx * 2, rPx * 2);
                }
        }

        g.setStroke(Color.BLACK);
        g.strokeRect(0.5, 0.5, vt.widthPx -1, vt.heightPx -1);

    }
    private void setSelectedSpecies(){
        this.selectedSpecies = (selectedSpeciesSupplier != null) ? selectedSpeciesSupplier.get() : Collections.emptySet();
    }

    public void setSelectedSpeciesSupplier(Supplier<Set<String>> supplier){
        this.selectedSpeciesSupplier = supplier;
    }
    public void enableBrushRemovalMode(Supplier<Double> brushSizeSupplier){
        forestCanvas.setOnMousePressed(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()/3));
        forestCanvas.setOnMouseDragged(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()/3));
    }
    public void enableBrushAgeMode(Supplier<Double> brushSizeSupplier, Supplier<Double> ageSupplier, TestGrid testGrid){
        forestCanvas.setOnMousePressed(e -> applyBrushAge(e.getX(), e.getY(), brushSizeSupplier.get()/3, ageSupplier.get(), testGrid));
        forestCanvas.setOnMouseDragged(e -> applyBrushAge(e.getX(), e.getY(), brushSizeSupplier.get()/3, ageSupplier.get(), testGrid));
    }
    public void disableBrushMode(){
        forestCanvas.setOnMousePressed(null);
        forestCanvas.setOnMouseDragged(null);
        forestCanvas.setOnMouseReleased(null);
    }

    private void applyBrushRemoval(double brushX, double brushY, double brushSize){
        //System.out.print("brush" + selectedSpecies);
        long brushRemovalStartTime = System.nanoTime();
        double brushRadiusPx = brushSizeToPixels(brushSize);
        setSelectedSpecies();
        List<Plant> toRemove = new ArrayList<>();
        for(Plant p : forest.getAllPlants()){
            if (!selectedSpecies.contains(p.getSpeciesName())){
                continue;
            }

            double xPx = vt.meterXtoPx(p.getX());
            double yPx = vt.meterYtoPx(p.getY());
            double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));
            //swapped so same as draw method
            double dx = brushX - yPx;
            double dy = brushY - xPx;
            double dist = Math.hypot(dx,dy);
            //if plant canopy contacts brush radius
            if(dist <= (brushRadiusPx + rPx)){
                toRemove.add(p);
            }
        }
        if (!toRemove.isEmpty()){
            for(Plant p : toRemove){
                forest.removePlant(p);
                //System.out.print("plant removed" + p);
            }
            drawForest();
            System.out.println("Apply Brush Removal Elapsed Time: " + (System.nanoTime() - brushRemovalStartTime) + " (nanoseconds). Removed " + toRemove.size() + " plants");
        } 
    }

    private void applyBrushAge(double brushX, double brushY, double brushSize, double ageFactor, TestGrid testGrid){
        long brushStartTime = System.nanoTime();
        double brushRadiusPx = brushSizeToPixels(brushSize);
        List<Plant> toChange = new ArrayList<>();
        double brushXMeters = vt.pxToMeterX(brushX);
        double brushYMeters = vt.pxToMeterY(brushY);
        double brushRadiusMeters = vt.pxToMeterX(brushRadiusPx);
        // bounding box in cell indices
        int minCellX = Math.max(0, (int)((brushXMeters - brushRadiusMeters) / gridSpacing));
        int maxCellX = Math.min(map.length - 1, (int)((brushXMeters + brushRadiusMeters) / gridSpacing));
        int minCellY = Math.max(0, (int)((brushYMeters - brushRadiusMeters) / gridSpacing));
        int maxCellY = Math.min(map[0].length - 1, (int)((brushYMeters + brushRadiusMeters) / gridSpacing));

        // --- Step 4: Loop through cells & check circular distance ---
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cy = minCellY; cy <= maxCellY; cy++) {
                // Find center of this cell in meters
                double cellXMeters = cx * gridSpacing + gridSpacing / 2.0;
                double cellYMeters = cy * gridSpacing + gridSpacing / 2.0;

                // Distance from brush center to this cell
                double dx = brushXMeters - cellXMeters;
                double dy = brushYMeters - cellYMeters;
                double dist = Math.hypot(dx, dy);

                // Apply only if inside circular brush
                if (dist <= brushRadiusMeters) {
                    testGrid.adjustAge(cx, cy, (float)ageFactor);
                }
            }
        }

        for(Plant p : forest.getAllPlants()){
            double xPx = vt.meterXtoPx(p.getX());
            double yPx = vt.meterYtoPx(p.getY());
            double rPx = Math.max(2.0, vt.metersToPx(p.getCanopyRadius()));
            //swapped so same as draw method
            double dx = brushX - yPx;
            double dy = brushY - xPx;
            double dist = Math.hypot(dx,dy);
            //if plant canopy contacts brush radius
            if(dist <= (brushRadiusPx + rPx)){
                toChange.add(p);
                //age change
            }
        }
        if (!toChange.isEmpty()){
            for(Plant p : toChange){
                int xCell = (int) (p.getX() / gridSpacing);
                int yCell = (int) (p.getY() / gridSpacing);
                testGrid.changePlantAge(xCell, yCell, (float)ageFactor, p);
            }
            drawForest();
            System.out.println("Brush Elapsed Time: " + (System.nanoTime() - brushStartTime) + " (nanoseconds). Changed ages of " + toChange.size() + " plants");
        } 
    }
    private double brushSizeToPixels(double size){
        return size * vt.cellPx;
    }

    @Override protected void layoutChildren() { mapCanvas.relocate(0, 0); forestCanvas.relocate(0, 0);}
}
