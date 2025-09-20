package plantsketch.ui;
import plantsketch.*;
/** Consistent transform for drawing grids and meter-based points. */
public final class ViewTransform {

// MAKING MAX PIXELS HERE SO THAT OUR GRIDS ARE ALWAYS THE SAME SIZE
    private final int maxPixels = 350;

    public final int dimX;          // columns (cells)
    public final int dimY;          // rows (cells)
    public final float gridSpacing; // meters per cell
    public final double cellPx;     // pixels per cell
    public final double widthPx;    // canvas width in pixels
    public final double heightPx;   // canvas height in pixels

    public ViewTransform(int dimX, int dimY, float gridSpacing) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.gridSpacing = gridSpacing;
        this.cellPx = Math.max(1, Math.floor((double) maxPixels / Math.max(dimX, dimY)));
        this.widthPx  = dimX * cellPx;
        this.heightPx = dimY * cellPx;
    }

    // --- cells → pixels (top-left origin) ---
    public double cellXtoPx(double cellX) { return cellX * cellPx; }
    public double cellYtoPx(double cellY) { return cellY * cellPx; }

    // --- meters → pixels (top-left origin) ---
    public double meterXtoPx(double meterX) { return (meterX / gridSpacing) * cellPx; }
    public double meterYtoPx(double meterY) { return (meterY / gridSpacing) * cellPx; }

    // --- meters → pixels for sizes (e.g., radius) ---
    public double metersToPx(double meters) { return (meters / gridSpacing) * cellPx; }
}
