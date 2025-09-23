package plantsketch.ui;

import plantsketch.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class ForestOnTerrainView extends Region {
    private Forest forest;
    private float[][] elevation; // [dimX][dimY]
    private ViewTransform vt;
    private Canvas canvas = new Canvas();
    //private Canvas overlay = new Canvas();
    private float gridSpacing;
    private boolean useDefaultCellSize;

    public ForestOnTerrainView(Forest forest, float[][] elevation, float gridSpacing) {
        this.forest = forest;
        this.elevation = elevation;
        int dimX = elevation.length;
        int dimY = elevation[0].length;
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
        //overlay.setLayoutX(0);
        //overlay.setLayoutY(0);
        //getChildren().addAll(canvas, overlay);
        getChildren().add(canvas);
        draw();
    }

    private void updateRegionSize(){
        setPrefSize(vt.widthPx, vt.heightPx);
        setMinSize(vt.widthPx, vt.heightPx);
        setMaxSize(vt.widthPx, vt.heightPx);
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
        //overlay.setWidth(vt.widthPx);
        //overlay.setHeight(vt.heightPx);
        requestLayout();
    }
    public void zoomIn(){
        int dimX = elevation.length;
        int dimY = elevation[0].length;
        //increase render by 20% (capped at 3000px)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.min(currentSize * 1.2, 3000);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        draw();
    }
    public void zoomOut(){
        int dimX = elevation.length;
        int dimY = elevation[0].length;
        //decrease render by 20% (capped at 80% of original)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        draw();
    }
    public void resetToDefault(){
        int dimX = elevation.length;
        int dimY = elevation[0].length;
        //reset
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);
        updateRegionSize();
        draw();
    }

    private static Color parseColour(String hexOrName) {
        try { return Color.web(hexOrName); } catch (Exception e) { return Color.LIMEGREEN; }
    }

    private void draw() {
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // 1) draw elevation as grayscale (cells)
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                float v = elevation[x][y];
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }
        double cs = vt.cellPx;
        for (int x = 0; x < vt.dimX; x++) {
            for (int y = 0; y < vt.dimY; y++) {
                double t = (elevation[x][y] - min) / Math.max(1e-9, (max - min));
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
    
    public void enableBrushRemovalMode(Supplier<Double> brushSizeSupplier){
        canvas.setOnMousePressed(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()));
        canvas.setOnMouseDragged(e -> applyBrushRemoval(e.getX(), e.getY(), brushSizeSupplier.get()));
        /* 
        overlay.setOnMouseMoved(e -> drawBrushOutline(e.getX(), e.getY(), brushSizeSupplier.get()));
        overlay.setOnMouseExited(e -> clearBrushOutline());
        */
    }

    public void disableBrushRemovalMode(){
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        /* 
        overlay.setOnMouseMoved(null);
        overlay.setOnMouseExited(null);
        clearBrushOutline();
        */
        
    }

    private void applyBrushRemoval(double brushX, double brushY, double brushSize){
        System.out.print("brush");
        //double px = vt.cellXtoPx(x);
        //double py = vt.cellYtoPx(y);
        double brushRadiusPx = brushSizeToPixels(brushSize);

        List<Plant> toRemove = new ArrayList<>();
        for(Plant p : forest.getAllPlants()){
            //System.out.println("x:" + x + "p: " + vt.meterXtoPx(p.getX()));
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
            System.out.print("removed " + toRemove.size() + " plants");
            draw();
        } else {
            System.out.println("none");
        }
    }

    private double brushSizeToPixels(double size){
        return size * vt.cellPx;
    }
    /* 
    private void drawBrushOutline(double x, double y, double brushSize){
        GraphicsContext g = overlay.getGraphicsContext2D();
        clearBrushOutline();
        g.setStroke(Color.RED);
        g.setLineWidth(2);
        double brushSizePx = brushSizeToPixels(brushSize);
        g.strokeOval(x-brushSizePx, y-brushSizePx, brushSizePx*2, brushSizePx*2);
    }



    private void clearBrushOutline(){
        overlay.getGraphicsContext2D().clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
    }
    */
    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
