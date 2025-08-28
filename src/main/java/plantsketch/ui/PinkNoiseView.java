package plantsketch.ui;

import plantsketch.PointSample;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;

public class PinkNoiseView extends Region {
    private final List<PointSample> samples;
    private final int dimX, dimY;
    private final Canvas canvas = new Canvas();

    public PinkNoiseView(List<PointSample> samples, int dimX, int dimY) {
        this.samples = samples;
        this.dimX = dimX;
        this.dimY = dimY;
        getChildren().add(canvas);
        draw();
        setPrefSize(canvas.getWidth(), canvas.getHeight());
    }

    private void draw() {
        if (samples == null)
            return;

        int rows = dimY, cols = dimX;
        int maxPixels = 800;
        double cell = Math.max(1, Math.floor((double) maxPixels / Math.max(rows, cols)));
        double w = cols * cell;
        double h = rows * cell;

        canvas.setWidth(w);
        canvas.setHeight(h);
        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setFill(Color.DEEPSKYBLUE);
        double r = Math.max(1, cell * 0.4); // point radius

        for (PointSample s : samples) {
            double x = s.getX() * cell;
            double y = s.getY() * cell;
            g.fillOval(x - r / 2, y - r / 2, r, r);
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
    }
}
