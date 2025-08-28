package plantsketch.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class GridHeatMapView extends Region {
    private final float[][] grid;
    private final Canvas canvas = new Canvas();

    public GridHeatMapView(float[][] grid) {
        this.grid = grid;
        getChildren().add(canvas);
        draw();
        // size to content
        setPrefSize(canvas.getWidth(), canvas.getHeight());
    }

    private void draw() {
        if (grid == null || grid.length == 0 || grid[0].length == 0)
            return;

        int rows = grid.length;
        int cols = grid[0].length;

        // fit into a reasonable pixel size (auto scale cell size)
        int maxPixels = 800; // longest side
        double cell = Math.max(1, Math.floor((double) maxPixels / Math.max(rows, cols)));
        double w = cols * cell;
        double h = rows * cell;

        canvas.setWidth(w);
        canvas.setHeight(h);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // min/max
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                float v = grid[r][c];
                if (v < min)
                    min = v;
                if (v > max)
                    max = v;
            }
        double range = Math.max(1e-12, max - min);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double t = (grid[r][c] - min) / range; // 0..1
                // grayscale (dark = low, bright = high) – invert if you want
                double gray = t;
                g.setFill(Color.color(gray, gray, gray));
                g.fillRect(c * cell, r * cell, cell, cell);
            }
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
    }
}
