package tugraz.ivis.parcoord.chart;

import javafx.beans.value.ChangeListener;
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

        setTickLabelFormatter();
    }

    /**
     * Registers a TickLabelFormatter which also correctly displays values for the inverted axis
     */
    private void setTickLabelFormatter() {
        axis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axis) {
            @Override
            public String toString(Number value) {
                // if the lower and upper bound were negated, the displayed value has to be fixed
                double val = value.doubleValue();
                if (inverted) {
                    val = -val;
                }

                // call original formatter with inverted value
                return super.toString(new Double(val));
            }
        });
    }


    /**
     * Inverts the axis by negating both lower and upper values
     * this seems to be the only efficient and easy way to correctly display the values
     */
    @SuppressWarnings("unchecked")
	public void invert() {
        double lower = axis.getLowerBound();
        double higher = axis.getUpperBound();
        double temp = lower;

        lower = -higher;
        higher = -temp;

        axis.setUpperBound(higher);
        axis.setLowerBound(lower);
        inverted = !inverted;
        
        if(filterSlider != null) {

            // adjust filters
            double filterHighTmp = 1.0 - filterLow;
            double filterLowTmp = 1.0 - filterHigh;
            
            // remove listeners and add them again afterwards
            ChangeListener<Number> highListener = (ChangeListener<Number>)filterSlider.getProperties().get("highListener");
            ChangeListener<Number> lowListener = (ChangeListener<Number>)filterSlider.getProperties().get("lowListener");
            filterSlider.highValueProperty().removeListener(highListener);
            filterSlider.lowValueProperty().removeListener(lowListener);

            filterLow = filterLowTmp;
            filterHigh = filterHighTmp;
            filterSlider.setLowValue(filterLowTmp);
            filterSlider.setHighValue(filterHighTmp);
            filterSlider.setLowValue(filterLowTmp);
            filterSlider.setHighValue(filterHighTmp);
            
            filterSlider.highValueProperty().addListener(highListener);
            filterSlider.lowValueProperty().addListener(lowListener);
        }
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
}
