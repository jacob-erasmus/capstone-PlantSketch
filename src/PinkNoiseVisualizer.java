import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PinkNoiseVisualizer extends JPanel {
    private List<PointSample> samples;
    private int width;
    private int height;

    public PinkNoiseVisualizer(List<PointSample> samples, int width, int height) {
        this.samples = samples;
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw a grid (optional, helps visualization)
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < width; i += 20) {
            g.drawLine(i, 0, i, height);
        }
        for (int j = 0; j < height; j += 20) {
            g.drawLine(0, j, width, j);
        }

        // Draw points
        g.setColor(Color.RED);
        for (PointSample sample : samples) {
            int x = (int) sample.getX();
            int y = (int) sample.getY();

            // flip y so origin is bottom-left instead of top-left
            int yInverted = height - y; 

            g.fillOval(x - 2, yInverted - 2, 4, 4);
        }
    }
}
