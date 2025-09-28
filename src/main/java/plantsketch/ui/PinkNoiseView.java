package plantsketch.ui;

import plantsketch.PointSample;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import java.util.List;

public class PinkNoiseView extends Region {
    private final List<PointSample> samples;
    private ViewTransform vt;
    private final Canvas canvas = new Canvas();
    float pointDiameter = 0.2f; // percentage of cell
    boolean useDefaultCellSize;
    int dimX;
    int dimY;
    float gridSpacing;

    public PinkNoiseView(List<PointSample> samples, int dimX, int dimY, float gridSpacing) {
        this.samples = samples;
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
    private void draw() {
        if (samples == null) return;

        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // background + frame
        g.setFill(Color.web("#0b1e2b"));
        g.fillRect(0, 0, vt.widthPx, vt.heightPx);
        g.setStroke(Color.GRAY);
        g.strokeRect(0.5, 0.5, vt.widthPx - 1, vt.heightPx - 1);

        // draw points (meters → pixels)
        g.setFill(Color.PINK);
        double r = Math.max(1, vt.cellPx * pointDiameter);
        for (PointSample s : samples) {
            double xPx = vt.meterXtoPx(s.getX());
            double yPx = vt.meterYtoPx(s.getY());
            // clamp for safety
            xPx = Math.max(0, Math.min(vt.widthPx - 1, xPx));
            yPx = Math.max(0, Math.min(vt.heightPx - 1, yPx));
            //g.fillOval(yPx - r / 2, xPx - r / 2, r, r);
             g.fillOval(xPx - r / 2, yPx - r / 2, r, r);
        }
    }

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
