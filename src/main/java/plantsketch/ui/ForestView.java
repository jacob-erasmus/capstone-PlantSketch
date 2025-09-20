package plantsketch.ui;

import plantsketch.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class ForestView extends Region {
    private final Forest forest;
    private final ViewTransform vt;
    private final Canvas canvas = new Canvas();
    private final double minCirclePx = 2.0; // keep plants visible

    public ForestView(Forest forest, int dimX, int dimY, float gridSpacing) {
        this.forest = forest;
        this.vt = new ViewTransform(dimX, dimY, gridSpacing);
        getChildren().add(canvas);
        draw();
        setPrefSize(canvas.getWidth(), canvas.getHeight());
    }

    private static Color parseColour(String hexOrName) {
        try { return Color.web(hexOrName); } catch (Exception e) { return Color.LIMEGREEN; }
    }

    private void draw() {
        canvas.setWidth(vt.widthPx);
        canvas.setHeight(vt.heightPx);
        GraphicsContext g = canvas.getGraphicsContext2D();

        // background
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, vt.widthPx, vt.heightPx);

        // draw plants per species map
        for (SpeciesMap sm : forest.getSpeciesMapList()) { // you may need to add a getter returning the list
            Color c = parseColour(sm.getSpecies().getColour());
            g.setFill(c.deriveColor(0, 1, 1, 0.85));

            for (Plant p : sm.getPlants()) {
                double xPx = vt.meterXtoPx(p.getX());
                double yPx = vt.meterYtoPx(p.getY());
                double rPx = Math.max(minCirclePx, vt.metersToPx(p.getCanopyRadius()));
                g.fillOval(xPx - rPx, yPx - rPx, rPx * 2, rPx * 2);
            }
        }

        g.setStroke(Color.GRAY);
        g.strokeRect(0.5, 0.5, vt.widthPx - 1, vt.heightPx - 1);
    }

    @Override protected void layoutChildren() { canvas.relocate(0, 0); }
}
