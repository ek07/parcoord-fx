package tugraz.ivis.parcoord.chart;

import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.controlsfx.control.RangeSlider;

public class ParallelCoordinatesAxis {
    private int id;
    private int axisIndex;
    private NumberAxis axis;
    private boolean inverted = false;
    private String label;
    private HBox labelBox;
    private RangeSlider filterSlider;
    private double filterHigh;
    private double filterLow;
    private Button button;


    public ParallelCoordinatesAxis(NumberAxis axis, int axisIndex, String label, HBox labelBox, RangeSlider filterSlider, Button button) {
        this.axis = axis;
        this.axisIndex = axisIndex;
        this.label = label;
        this.labelBox = labelBox;
        this.filterSlider = filterSlider;
        this.id = axisIndex; // set the initial position as the index

        if (filterSlider != null) {
            filterHigh = filterSlider.getHighValue();
            filterLow = filterSlider.getLowValue();
        }

        this.button = button;

        axis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axis) {
            @Override
            public String toString(Number value) {
                // note we are printing minus value
                double val = value.doubleValue();
                if (inverted) {
                    val = -val;
                }

                return super.toString(new Double(val));//new Double(val));
            }
        });
    }


    public NumberAxis getAxis() {
        return axis;
    }

    public boolean isInverted() {
        return inverted;
    }

    public int getAxisIndex() {
        return axisIndex;
    }

    public void setAxisIndex(int axisIndex) {
        this.axisIndex = axisIndex;
    }

    public String getLabel() {
        return label;
    }

    public HBox getLabelBox() {
        return labelBox;
    }

    public RangeSlider getFilterSlider() {
        return filterSlider;
    }

    public double getFilterHigh() {
        return filterHigh;
    }

    public void setFilterHigh(double filterHigh) {
        this.filterHigh = filterHigh;
    }

    public double getFilterLow() {
        return filterLow;
    }

    public void setFilterLow(double filterLow) {
        this.filterLow = filterLow;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }


    public int getId() {
        return id;
    }

    public void invert() {
        double lower = axis.getLowerBound();
        double higher = axis.getUpperBound();
        double temp = lower;

        lower = -higher;
        higher = -temp;

        axis.setUpperBound(higher);
        axis.setLowerBound(lower);
        inverted = !inverted;
    }
}
