package plantsketch.ui;

import plantsketch.*;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SidePanel extends Region{
    private final VBox parameterPanel = new VBox(10);
    private final Map<String, CheckBox> speciesCheck = new HashMap<>();

    public VBox buildParameterPanel(){
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-font-family: Consolas, 'Courier New', monospace; -fx-font-size: 12px;");
        
        Label title = new Label("Parameter Controls");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(parameterPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        // Create species checkboxes
        speciesCheck.put("boxwood", new CheckBox("Boxwood"));
        speciesCheck.put("snowyMespilus", new CheckBox("Snowy Mespilus"));
        speciesCheck.put("mountainPine", new CheckBox("Mountain Pine"));
        speciesCheck.put("silverFir", new CheckBox("Silver Fir"));
        speciesCheck.put("silverBirch", new CheckBox("Silver Birch"));
        speciesCheck.put("sissileOak", new CheckBox("Sissile Oak"));
        speciesCheck.put("europeanBeech", new CheckBox("European Beech"));

        Label temp = new Label("Temperature");
        Slider tempSlider = new Slider(-10, 40, -10);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setBlockIncrement(0.5);
        tempSlider.setPrefWidth(250);
        
        Label enivroAge = new Label("Enivornment Age");
        Slider ageSlider = new Slider(1, 600, 1);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setBlockIncrement(1);
        ageSlider.setPrefWidth(250);

        Label sun = new Label("Sun");
        Slider sunSlider = new Slider(3, 7, 3);
        sunSlider.setShowTickLabels(true);
        sunSlider.setShowTickMarks(true);
        sunSlider.setMajorTickUnit(0.25);
        sunSlider.setBlockIncrement(0.25);
        sunSlider.setSnapToTicks(true);
        sunSlider.setMinorTickCount(0);
        sunSlider.showTickLabelsProperty();
        sunSlider.setPrefWidth(250);

        GridPane gridPane = new GridPane();
        int col = 0, row = 0;
        for (CheckBox boxes : speciesCheck.values()) {
            boxes.setSelected(true);
            gridPane.add(boxes, col, row);
            col++;
            if (col > 1) { // wrap after 2 columns
                col = 0;
                row++;
            }
        }

        Label brushSize = new Label("Brush Size");
        Slider brushSizeSlider = new Slider(1, 5, 1);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setMajorTickUnit(1);
        brushSizeSlider.setBlockIncrement(1);
        brushSizeSlider.setPrefWidth(250);
        brushSizeSlider.setSnapToTicks(true);
        brushSizeSlider.setMinorTickCount(0);
        brushSizeSlider.showTickLabelsProperty();
        

        Button simulateBtn = new Button("Remove species");
        simulateBtn.setPrefWidth(250);

        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            title,
            new Separator(),
            temp,
            tempSlider,
            new Separator(),
            enivroAge,
            ageSlider,
            new Separator(),
            sun,
            sunSlider,
            new Separator(),
            gridPane,
            new Separator(),
            brushSize,
            brushSizeSlider,
            new Separator(),
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        
        VBox container = new VBox(scrollPane);
        return container;

    }

    public SimulationResult removeSpecies(SimulationResult result){
        for (CheckBox boxes : speciesCheck.values()) {
            if(boxes.isSelected()!=true){
                result.forest().removeSpecies(boxes.getText());
            }
        }
        return result;
    }
}
