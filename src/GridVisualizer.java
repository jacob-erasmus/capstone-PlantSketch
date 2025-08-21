import javax.swing.*;
import java.awt.*;

public class GridVisualizer extends JPanel {

    private float[][] grid;
    private int cellSize = 10; // pixels per cell

    public GridVisualizer(float[][] grid) {
        this.grid = grid;
        //setPreferredSize(new Dimension(grid.length * cellSize, grid[0].length * cellSize));

        setPreferredSize(new Dimension(grid[0].length * cellSize, grid.length * cellSize));

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // find min and max for normalization
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                if (grid[x][y] < min) min = grid[x][y];
                if (grid[x][y] > max) max = grid[x][y];
            }
        }

        // draw each cell
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                float value = grid[x][y];
                int intensity = (int) ((value - min) / (max - min) * 255); // scale 0–255

                // invert if you want dark = older (max -> 0, min -> 255)
                intensity = 255 - intensity;

                // greyscale color
                Color color = new Color(intensity, intensity, intensity);
                g.setColor(color);

                g.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);
            }
        }

    }

    // quick test method
    public static void showGrid(float[][] grid, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(new GridVisualizer(grid)));
        frame.pack();
        frame.setVisible(true);
    }
}
