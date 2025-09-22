package plantsketch.ui;

// this class is to edit the sliders in the run interface that will then be loaded onto the brush
// I DONT THINK I ACTUALLY NEED IT

import javafx.scene.control.Slider;

public class SliderEditor {
    
    private String paramName;
    private float min;
    private float max;
    private Slider slider;
    private int chosenValue;

    public SliderEditor(String paramName, float min, float max, Slider slider) 
    {
        this.paramName = paramName;
        this.min = min;
        this.max = max;
        this.slider = slider;
    }

    public void setValue(int newValue)
    {
        this.chosenValue = newValue;
    }

    public int getCurrentValue()
    {
        return chosenValue;
    }

    public float readInValue()
    {
        return (float)slider.getValue();
    }

}
