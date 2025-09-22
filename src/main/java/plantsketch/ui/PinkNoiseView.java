package plantsketch.ui;

import plantsketch.PointSample;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import java.util.List;

public class PinkNoiseView extends Region {
    private final List<PointSample> samples;
    private final ViewTransform vt;
    private final Canvas canvas = new Canvas();
    float pointDiameter = 0.2f; // percentage of cell

    public PinkNoiseView(List<PointSample> samples, int dimX, int dimY, float gridSpacing) {
        this.samples = samples;
        this.vt = new ViewTransform(dimX, dimY, gridSpacing); // choose a max size you like
        getChildren().add(canvas);
        draw();
        setPrefSize(canvas.getWidth(), canvas.getHeight());
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
        double r = Math.max(1, vt.cellPx * pointDiameter); // point diameter ~40% of a cell
        for (PointSample s : samples) {
            double xPx = vt.meterXtoPx(s.getX());
            double yPx = vt.meterYtoPx(s.getY());
            // clamp for safety
            xPx = Math.max(0, Math.min(vt.widthPx - 1, xPx));
            yPx = Math.max(0, Math.min(vt.heightPx - 1, yPx));
            g.fillOval(yPx - r / 2, xPx - r / 2, r, r);
            // swapping:    g.fillOval(xPx - r / 2, yPx - r / 2, r, r);
        }
    }

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
