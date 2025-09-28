package plantsketch.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

//tracks console for dubugging purposes. Used LLM generated code as non-functional related feature, used for debugging


public final class ConsolePane {

    // TextArea is a JavaFX UI component that displays multi-line text (like a text box)
    private final TextArea area = new TextArea();

    public ConsolePane() {
        area.setEditable(false); // Users cannot type in this text area
        area.setWrapText(false); // Long lines won't wrap to next line
        // CSS styling to make it look like a console with monospace font
        area.setStyle("-fx-font-family: Consolas, 'Courier New', monospace; -fx-font-size: 12px;");
    }

    // Returns the UI component so it can be added to the JavaFX scene
    public Node getNode() { return area; }

    public void clear() { area.clear(); }

    public void log(String s) {
        if (s == null) return;
        // JavaFX requires UI updates to happen on the "FX Application Thread"
        if (Platform.isFxApplicationThread()) {
            area.appendText(s + System.lineSeparator());
        } else {
            // If called from another thread, schedule the UI update on the correct thread
            Platform.runLater(() -> area.appendText(s + System.lineSeparator()));
        }
    }

    // Redirects System.out and System.err to display in this console pane
    public void hookSystemStreams() {
        PrintStream outPs = new PrintStream(new TextAreaOutputStream(area), true, StandardCharsets.UTF_8);
        System.setOut(outPs); // All System.out.println() calls will now show in the console
        PrintStream errPs = new PrintStream(new TextAreaOutputStream(area), true, StandardCharsets.UTF_8);
        System.setErr(errPs); // All System.err.println() calls will now show in the console
    }

    /** OutputStream that appends to the TextArea on the JavaFX Application Thread. */
    private static final class TextAreaOutputStream extends OutputStream {
        private final TextArea area;
        // Buffer to collect text before updating the UI (improves performance)
        private final StringBuilder buffer = new StringBuilder();

        TextAreaOutputStream(TextArea area) { this.area = area; }

        // Called when a single byte is written (like from System.out.print())
        @Override public void write(int b) {
            char c = (char) (b & 0xFF); // Convert byte to character
            buffer.append(c);
            if (c == '\n') flushBuffer(); // Update UI when we hit a newline
        }

        // Called when multiple bytes are written at once (more efficient)
        @Override public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            buffer.append(s);
            int idx;
            // Process each complete line immediately to keep console responsive
            while ((idx = buffer.indexOf("\n")) >= 0) {
                String line = buffer.substring(0, idx + 1);
                append(line);
                buffer.delete(0, idx + 1); // Remove processed line from buffer
            }
        }

        @Override public void flush() { flushBuffer(); }

        // Send any remaining buffered text to the UI
        private void flushBuffer() {
            if (buffer.length() > 0) {
                String s = buffer.toString();
                append(s);
                buffer.setLength(0); // Clear the buffer
            }
        }

        // Safely update the TextArea from any thread
        private void append(String s) {
            if (s == null || s.isEmpty()) return;
            if (Platform.isFxApplicationThread()) {
                area.appendText(s); // Already on UI thread, update directly
            } else {
                // Schedule UI update on the correct thread
                Platform.runLater(() -> area.appendText(s));
            }
        }
    }
}
