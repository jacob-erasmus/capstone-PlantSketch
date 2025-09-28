package plantsketch.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

//tracks console for dubugging purposes. Used LLM generated code

public final class ConsolePane {

    private final TextArea area = new TextArea();

    public ConsolePane() {
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: Consolas, 'Courier New', monospace; -fx-font-size: 12px;");
    }

    public Node getNode() { return area; }

    public void clear() { area.clear(); }

    public void log(String s) {
        if (s == null) return;
        if (Platform.isFxApplicationThread()) {
            area.appendText(s + System.lineSeparator());
        } else {
            Platform.runLater(() -> area.appendText(s + System.lineSeparator()));
        }
    }

    //get the system streams
    public void hookSystemStreams() {
        PrintStream outPs = new PrintStream(new TextAreaOutputStream(area), true, StandardCharsets.UTF_8);
        System.setOut(outPs);
        PrintStream errPs = new PrintStream(new TextAreaOutputStream(area), true, StandardCharsets.UTF_8);
        System.setErr(errPs);
    }

    /** OutputStream that appends to the TextArea on the JavaFX Application Thread. */
    private static final class TextAreaOutputStream extends OutputStream {
        private final TextArea area;
        private final StringBuilder buffer = new StringBuilder();

        TextAreaOutputStream(TextArea area) { this.area = area; }

        @Override public void write(int b) {
            char c = (char) (b & 0xFF);
            buffer.append(c);
            if (c == '\n') flushBuffer();
        }

        @Override public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            buffer.append(s);
            int idx;
            while ((idx = buffer.indexOf("\n")) >= 0) {
                String line = buffer.substring(0, idx + 1);
                append(line);
                buffer.delete(0, idx + 1);
            }
        }

        @Override public void flush() { flushBuffer(); }

        private void flushBuffer() {
            if (buffer.length() > 0) {
                String s = buffer.toString();
                append(s);
                buffer.setLength(0);
            }
        }

        private void append(String s) {
            if (s == null || s.isEmpty()) return;
            if (Platform.isFxApplicationThread()) {
                area.appendText(s);
            } else {
                Platform.runLater(() -> area.appendText(s));
            }
        }
    }
}
