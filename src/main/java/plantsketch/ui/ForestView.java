package plantsketch.ui;

import plantsketch.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class ForestView extends Region {
    private final Forest forest;
    private ViewTransform vt;
    private final Canvas canvas = new Canvas();
    private final double minCirclePx = 2.0; // keep plants visible
    boolean useDefaultCellSize;
    int dimX;
    int dimY;
    float gridSpacing;
    private Map<String, Color> colorCache = new HashMap<>();


    public ForestView(Forest forest, int dimX, int dimY, float gridSpacing) {
        this.forest = forest;
        this.dimX = dimX;
        this.dimY = dimY;
        this.gridSpacing = gridSpacing;
        useDefaultCellSize = true;
        if (dimX < 256){
            useDefaultCellSize = false;
        }
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
        //increase render by 20% (capped at 3000px)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.min(currentSize * 1.2, 3000);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        draw();
    }
    public void zoomOut(){
        //decrease render by 20% (capped at 80% of original)
        double currentSize = Math.max(vt.widthPx, vt.heightPx);
        double newSize = Math.max(currentSize / 1.2, 256);
        this.vt = new ViewTransform(dimX, dimY, gridSpacing, newSize, newSize, useDefaultCellSize);
        updateRegionSize();
        draw();
    }
    public void resetToDefault(){
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

        // background
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, vt.widthPx, vt.heightPx);

        // draw plants per species map
        for (SpeciesMap sm : forest.getSpeciesMapList()) { // you may need to add a getter returning the list
            Color c = colorCache.computeIfAbsent(sm.getSpecies().getColour(),
                colorStr -> parseColour(colorStr).deriveColor(0, 1, 1, 0.85));
            g.setFill(c);

            for (Plant p : sm.getPlants()) {
                double xPx = vt.meterXtoPx(p.getX());
                double yPx = vt.meterYtoPx(p.getY());
                double rPx = Math.max(minCirclePx, vt.metersToPx(p.getCanopyRadius()));
                //g.fillOval(yPx - rPx, xPx - rPx, rPx * 2, rPx * 2);
                g.fillOval(xPx - rPx, yPx - rPx, rPx * 2, rPx * 2);
            }
        }

        g.setStroke(Color.GRAY);
        g.strokeRect(0.5, 0.5, vt.widthPx - 1, vt.heightPx - 1);
    }

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
