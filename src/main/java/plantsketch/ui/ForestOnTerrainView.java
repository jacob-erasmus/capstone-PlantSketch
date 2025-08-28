package plantsketch.ui;

import plantsketch.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ForestOnTerrainView extends Region {
    private final Forest forest;
    private final float[][] elevation;
    private final Canvas canvas = new Canvas();

    public ForestOnTerrainView(Forest forest, float[][] elevation) {
        this.forest = forest;
        this.elevation = elevation;
        getChildren().add(canvas);
        draw();
        setPrefSize(canvas.getWidth(), canvas.getHeight());
    }

    private void draw() {
        if (elevation == null)
            return;
        int rows = elevation.length;
        int cols = elevation[0].length;

        int maxPixels = 800;
        double cell = Math.max(1, Math.floor((double) maxPixels / Math.max(rows, cols)));
        double w = cols * cell;
        double h = rows * cell;

        canvas.setWidth(w);
        canvas.setHeight(h);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // draw elevation grayscale
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                float v = elevation[r][c];
                if (v < min)
                    min = v;
                if (v > max)
                    max = v;
            }
        double range = Math.max(1e-12, max - min);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double t = (elevation[r][c] - min) / range;
                g.setFill(Color.gray(t));
                g.fillRect(c * cell, r * cell, cell, cell);
            }
        }

        // overlay plants
        var plants = new ArrayList<Plant>(forest.getAllPlants());
        for (Plant p : plants) {
            double x = p.getX() * cell;
            double y = p.getY() * cell;
            double d = Math.max(2.0, p.getCanopyRadius() * 2.0 * cell);
            g.setFill(Color.web("#00ff00", 0.7)); // translucent green by default
            // try species color if valid
            try {
                g.setFill(Color.web(p.getColour(), 0.7));
            } catch (Exception ignored) {
            }
            g.fillOval(x - d / 2, y - d / 2, d, d);
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
    }
}
