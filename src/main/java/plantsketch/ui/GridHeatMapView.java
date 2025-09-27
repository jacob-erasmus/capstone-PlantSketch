package plantsketch.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class GridHeatMapView extends Region {
    private final float[][] grid;
    private final Canvas canvas = new Canvas();
    private final float gridMin, gridMax, gridRange;

    // Pre-computed grayscale colors to avoid creating new Color objects
    private static final Color[] GRAYSCALE = new Color[256];
    static {
        for (int i = 0; i < 256; i++) {
            double gray = i / 255.0;
            GRAYSCALE[i] = Color.color(gray, gray, gray);
        }
    }

    public GridHeatMapView(float[][] grid) {
        this.grid = grid;

        // Calculate min/max once in constructor instead of every draw()
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float[] row : grid) {
            for (float val : row) {
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }
        this.gridMin = min;
        this.gridMax = max;
        this.gridRange = Math.max(1e-12f, max - min);

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

        // Draw using cached min/max and pre-computed colors
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double t = (grid[r][c] - gridMin) / gridRange; // 0..1
                int grayIndex = Math.max(0, Math.min(255, (int) Math.round(t * 255)));
                g.setFill(GRAYSCALE[grayIndex]);
                g.fillRect(c * cell, r * cell, cell, cell);
            }
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
    }
}
