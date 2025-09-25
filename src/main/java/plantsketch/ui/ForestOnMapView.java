package plantsketch.ui;

import plantsketch.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
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
        //renderzoom
        updateRegionSize();
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        getChildren().add(canvas);
        draw();
    }

    private void updateRegionSize(){
        setPrefSize(vt.widthPx, vt.heightPx);
        setMinSize(vt.widthPx, vt.heightPx);
        setMaxSize(vt.widthPx, vt.heightPx);
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
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
        draw();
    }
    public void zoomOut(){
        int dimX = map.length;
        int dimY = map[0].length;
        //decrease render by 20% (capped at 256)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        draw();
    }
    public void resetToDefault(){
        int dimX = map.length;
        int dimY = map[0].length;
        //reset
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);
        updateRegionSize();
        draw();
    }

    private static Color parseColour(String hexOrName) {
        try { return Color.web(hexOrName); } catch (Exception e) { return Color.LIMEGREEN; }
    }

    public void draw() {
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // 1) draw elevation as grayscale (cells)
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                float v = map[x][y];
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }
        double cs = vt.cellPx;
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                double t = (map[x][y] - min) / Math.max(1e-9, (max - min));
                int gray = (int) Math.round(t * 255);
                g.setFill(Color.rgb(gray, gray, gray));
                
                double px = vt.cellXtoPx(x);
                double py = vt.cellYtoPx(y);
                g.fillRect(py, px, cs, cs);
                // swapping them to fix mirror image thing:   g.fillRect(px, py, cs, cs);
            }
        }

        // 2) draw plants on top (meters)
        for (SpeciesMap sm : forest.getSpeciesMapList()) {
                Color c = parseColour(sm.getSpecies().getColour()).deriveColor(0, 1, 1, 0.85);
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
        g.strokeRect(0.5, 0.5, vt.widthPx - 1, vt.heightPx - 1);
    }
    
    private void setSelectedSpecies(){
        this.selectedSpecies = (selectedSpeciesSupplier != null) ? selectedSpeciesSupplier.get() : Collections.emptySet();
    }

    public void setSelectedSpeciesSupplier(Supplier<Set<String>> supplier){
        this.selectedSpeciesSupplier = supplier;
    }
    public void enableBrushRemovalMode(Supplier<Double> brushSizeSupplier){
        canvas.setOnMousePressed(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()));
        canvas.setOnMouseDragged(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()));
    }

    public void disableBrushRemovalMode(){
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
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
            draw();
            System.out.println("Apply Brush Removal Elapsed Time: " + (System.nanoTime() - brushRemovalStartTime) + " (nanoseconds). Removed " + toRemove.size() + " plants");
        } 
    }

    private double brushSizeToPixels(double size){
        return size * vt.cellPx;
    }

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
