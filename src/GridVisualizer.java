import javax.swing.*;
import java.awt.*;

public class GridVisualizer extends JPanel {
    private float[][] grid;
    private int cellSize; // dynamically computed

    public GridVisualizer(float[][] grid) {
        this.grid = grid;

        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int halfSize = Math.min(screenSize.width, screenSize.height) / 2; // half of smaller dimension

        // Find the larger grid dimension
        int gridMax = Math.max(grid.length, grid[0].length);

        // Compute cell size so that the grid fits in half-screen square
        cellSize = Math.max(1, halfSize / gridMax);


        // Set preferred size
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
                int intensity = (int) ((value - min) / (max - min) * 255);
                intensity = 255 - intensity; // invert
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
        frame.setLocationRelativeTo(null); // center window
        frame.setVisible(true);
    }
}
