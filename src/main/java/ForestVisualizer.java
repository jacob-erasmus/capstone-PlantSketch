import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ForestVisualizer extends JPanel {
    private final Forest forest;
    private final int gridWidth;   // in cells
    private final int gridHeight;  // in cells
    private final int cellSize;    // pixels per cell

    public ForestVisualizer(Forest forest, int gridWidth, int gridHeight, int cellSize) {
        this.forest = forest;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(gridWidth * cellSize, gridHeight * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Smooth circles
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Snapshot + de-duplicate plants by id (Forest.getAllPlants() appends each call)
        List<Plant> all = forest.getAllPlants();
        Map<Integer, Plant> byId = new LinkedHashMap<>();
        for (Plant p : all) {
            byId.put(p.getId(), p);
        }

        for (Plant plant : byId.values()) {
            // Convert grid coords → pixels
            float gx = plant.getX();
            float gy = plant.getY();
            float rCells = Math.max(plant.getCanopyRadius(), 0f);

            int px = Math.round(gx * cellSize);
            int py = Math.round(gy * cellSize);
            int rPx = Math.max(1, Math.round(rCells * cellSize));     // minimum 1px radius
            int dPx = Math.max(2, 2 * rPx);                            // minimum 2px diameter

            // Centered circle
            int drawX = px - rPx;
            int drawY = py - rPx;

            // Fill with species colour
            Color fill = parseColor(plant.getColour());
            g2.setColor(fill);
            g2.fillOval(drawX, drawY, dPx, dPx);

            // Optional outline
            g2.setColor(Color.BLACK);
            g2.drawOval(drawX, drawY, dPx, dPx);
        }

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
                default: return Color.MAGENTA; // fallback if unknown
            }
        } catch (Exception e) {
            return Color.MAGENTA;
        }
    }

    public static void showForest(Forest forest, int gridWidth, int gridHeight, int cellSize) {
        JFrame frame = new JFrame("Forest Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(new ForestVisualizer(forest, gridWidth, gridHeight, cellSize)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

