package plantsketch.ui;

import plantsketch.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ForestView extends Region {
    private final Forest forest;
    private final int dimX, dimY;
    private final double scale; // pixels per cell
    private final Canvas canvas = new Canvas();

    public ForestView(Forest forest, int dimX, int dimY, double scale) {
        this.forest = forest;
        this.dimX = dimX;
        this.dimY = dimY;
        this.scale = scale;
        getChildren().add(canvas);
        draw();
        setPrefSize(canvas.getWidth(), canvas.getHeight());
    }

    private void draw() {
        double w = dimX * scale;
        double h = dimY * scale;
        canvas.setWidth(w);
        canvas.setHeight(h);

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        var plants = new ArrayList<Plant>(forest.getAllPlants());
        for (Plant p : plants) {
            Color color = parseColour(p.getColour());
            g.setFill(color.deriveColor(0, 1, 1, 0.8));
            double x = p.getX() * scale;
            double y = p.getY() * scale;
            double radiusPixels = p.getCanopyRadius() * scale; // canopy radius in "cells" -> pixels
            double d = Math.max(2.0, radiusPixels * 2.0);
            g.fillOval(x - d / 2, y - d / 2, d, d);
        }
    }

    private Color parseColour(String name) {
        if (name == null)
            return Color.FORESTGREEN;
        switch (name.toLowerCase()) {
            case "red":
                return Color.RED;
            case "blue":
                return Color.BLUE;
            case "green":
                return Color.GREEN;
            case "yellow":
                return Color.YELLOW;
            case "orange":
                return Color.ORANGE;
            case "purple":
                return Color.PURPLE;
            default:
                try {
                    return Color.web(name);
                } catch (Exception ignored) {
                }
                return Color.FORESTGREEN;
        }
    }

    @Override
    protected void layoutChildren() {
        canvas.relocate(0, 0);
    }
}
