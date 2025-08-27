import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ForestOnTerrainVisualizer extends JPanel {
    private final Forest forest;
    private final float[][] elevationGrid;
    private final int cellSize;

    public ForestOnTerrainVisualizer(Forest forest, float[][] elevationGrid) {
        this.forest = forest;
        this.elevationGrid = elevationGrid;

        // Compute cell size to fit grid to half of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int halfSize = Math.min(screenSize.width, screenSize.height) / 2;
        int gridMax = Math.max(elevationGrid.length, elevationGrid[0].length);
        cellSize = Math.max(1, halfSize / gridMax);

        setPreferredSize(new Dimension(elevationGrid[0].length * cellSize, elevationGrid.length * cellSize));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int gridRows = elevationGrid.length;
        int gridCols = elevationGrid[0].length;

        // --- Draw elevation grid ---
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        for (int x = 0; x < gridRows; x++) {
            for (int y = 0; y < gridCols; y++) {
                if (elevationGrid[x][y] < min) min = elevationGrid[x][y];
                if (elevationGrid[x][y] > max) max = elevationGrid[x][y];
            }
        }

        for (int x = 0; x < gridRows; x++) {
            for (int y = 0; y < gridCols; y++) {
                float val = elevationGrid[x][y];
                int intensity = (int)((val - min) / (max - min) * 255);
                intensity = 255 - intensity; // invert so higher elevation = darker
                g2.setColor(new Color(intensity, intensity, intensity));
                int px = Math.round(y / (float) gridCols * getWidth());
                int py = Math.round(x / (float) gridRows * getHeight());
                int nextPx = Math.round((y + 1) / (float) gridCols * getWidth());
                int nextPy = Math.round((x + 1) / (float) gridRows * getHeight());
                g2.fillRect(px, py, nextPx - px, nextPy - py);
            }
        }

        // --- Draw trees ---
        List<Plant> all = forest.getAllPlants();
        Map<Integer, Plant> byId = new LinkedHashMap<>();
        for (Plant p : all) byId.put(p.getId(), p);

        for (Plant plant : byId.values()) {
            float gx = plant.getX();
            float gy = plant.getY();
            float rCells = Math.max(plant.getCanopyRadius(), 0f);

            int px = Math.round(gx / gridCols * getWidth());
            int py = Math.round(gy / gridRows * getHeight());
            int rPx = Math.max(1, Math.round(rCells / gridCols * getWidth())); // scale radius
            int dPx = Math.max(2, 2 * rPx);

            int drawX = Math.max(0, Math.min(px - rPx, getWidth() - dPx));
            int drawY = Math.max(0, Math.min(py - rPx, getHeight() - dPx));

            Color fill = parseColor(plant.getColour());
            g2.setColor(fill);
            g2.fillOval(drawX, drawY, dPx, dPx);

            g2.setColor(Color.BLACK);
            g2.drawOval(drawX, drawY, dPx, dPx);
        }

        // --- OPTIONAL: Draw river example ---
        // g2.setColor(Color.BLUE);
        // g2.setStroke(new BasicStroke(3));
        // g2.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);

        g2.dispose();
    }

    private Color parseColor(String s) {
        if (s == null) return Color.MAGENTA;
        s = s.trim();
        try {
            if (s.startsWith("#")) return Color.decode(s);
            switch (s.toLowerCase()) {
                case "red": return Color.RED;
                case "green": return Color.GREEN;
                case "blue": return Color.BLUE;
                case "yellow": return Color.YELLOW;
                case "orange": return Color.ORANGE;
                case "pink": return Color.PINK;
                case "gray":
                case "grey": return Color.GRAY;
                case "black": return Color.BLACK;
                case "white": return Color.WHITE;
                default: return Color.MAGENTA;
            }
        } catch (Exception e) {
            return Color.MAGENTA;
        }
    }

    public static void showForestOnTerrain(Forest forest, float[][] elevationGrid) {
        JFrame frame = new JFrame("Forest on Elevation Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(new ForestOnTerrainVisualizer(forest, elevationGrid)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
