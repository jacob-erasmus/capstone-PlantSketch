package plantsketch.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;

public class GridEditor extends VBox {
        private String paramName;
        private float min;
        private float max;
        private TextField[][] fields = new TextField[2][2];
        private float[][] originalValues;
        
        public GridEditor(String paramName, float min, float max) {
            this.paramName = paramName;
            this.min = min;
            this.max = max;
            
            setSpacing(5);
            
            Label label = new Label(paramName);
            label.setFont(Font.font("System", FontWeight.BOLD, 12));
            getChildren().add(label);
            
            GridPane grid = new GridPane();
            grid.setHgap(5);
            grid.setVgap(5);
            
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    fields[i][j] = new TextField();
                    fields[i][j].setPrefWidth(80);
                    fields[i][j].setPromptText(String.format("[%d,%d]", i+1, j+1));
                    grid.add(fields[i][j], j, i);
                }
            }
            
            Label rangeLabel = new Label(String.format("Range: %.1f - %.1f", min, max));
            rangeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
            
            getChildren().addAll(grid, rangeLabel);
        }

        public void setValues(float[][] values) {
            if (values != null && values.length >= 2) {
                originalValues = new float[2][2];
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        fields[i][j].setText(String.format("%.2f", values[j][i]));
                        originalValues[i][j] = values[i][j];
                    }
                }
            }
        }

        public boolean isEdited() {
            if (originalValues == null) return false;
            float[][] current = getValues();
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    if (Math.abs(current[i][j] - originalValues[i][j]) > 1e-6) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public float[][] getValues() {
            float[][] values = new float[2][2];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    try {
                        float val = Float.parseFloat(fields[j][i].getText());
                        values[i][j] = Math.max(min, Math.min(max, val));
                    } catch (NumberFormatException e) {
                        values[i][j] = (min + max) / 2;
                    }
                }
            }
            return values;
        }
    }
