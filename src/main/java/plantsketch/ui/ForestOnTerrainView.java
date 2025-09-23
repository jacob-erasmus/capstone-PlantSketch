package plantsketch.ui;

import plantsketch.*;
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
    private float gridSpacing;

    public ForestOnTerrainView(Forest forest, float[][] elevation, float gridSpacing) {
        this.forest = forest;
        this.elevation = elevation;
        int dimX = elevation.length;
        int dimY = elevation[0].length;
        boolean useDefaultCellSize = true;
        if (dimX < 256){
            useDefaultCellSize = false;
        }
        this.gridSpacing = gridSpacing;
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, dimX, dimY, useDefaultCellSize);
        getChildren().add(canvas);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //listen for size changes
        widthProperty().addListener((obs, oldVal, newVal) -> { if (newVal.doubleValue() > 0) { updateSize(); draw();}});
        heightProperty().addListener((obs, oldVal, newVal) -> { if (newVal.doubleValue() > 0) { updateSize(); draw();}});
        draw();
    }

    private void updateSize(){
        double width = getWidth();
        double height = getHeight();
        if(width > 0 && height > 0){
            //scale to fit viewtransform
            int dimX = elevation.length;
            int dimY = elevation[0].length;
            this.vt = new ViewTransform(dimX, dimY, gridSpacing, width, height, true);
            // Update canvas size
            canvas.setWidth(vt.widthPx);
            canvas.setHeight(vt.heightPx);
            canvas.setLayoutX(0);
            canvas.setLayoutY(0);
            setPrefSize(vt.widthPx, vt.heightPx);
            // Redraw with new dimensions
            draw();
        }
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

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
