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
    public void invert() {
        double lower = axis.getLowerBound();
        double higher = axis.getUpperBound();
        double temp = lower;

        lower = -higher;
        higher = -temp;

        axis.setUpperBound(higher);
        axis.setLowerBound(lower);

        if (filterSlider != null) {
            /*
             * TODO: discuss with thomas if necessary and if possible to get working
            double oldHighV = filterSlider.getHighValue();
            double oldLowV = filterSlider.getLowValue();
            System.out.println("===");
            System.out.println("oldHighVal:"+oldHighV+"|oldLowVal:"+oldLowV);
            filterLow = 1 - oldHighV;
            filterHigh = 1 - oldLowV;
            System.out.println("newHigh:"+filterHigh+"|oldLowVal:"+filterLow);
            filterSlider.setHighValue(filterHigh);
            filterSlider.setLowValue(filterLow);
            System.out.println("aftersetHigh:"+filterSlider.getHighValue()+"|afterSetLow:"+filterSlider.getLowValue());*/
            // for now, just reset filters
            //filterSlider.setLowValue(0.0);
            //filterSlider.setHighValue(1.0);
        }

        inverted = !inverted;
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
