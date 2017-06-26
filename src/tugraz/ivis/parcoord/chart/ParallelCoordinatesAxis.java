package tugraz.ivis.parcoord.chart;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.RangeSlider;

import java.util.Map;

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
    private Button btnInvert;
    private Button btnLeft;
    private Button btnRight;

    /**
     * A basic constructor which allows for initial creation of the object
     *
     * @param axisIndex
     */
    public ParallelCoordinatesAxis(int axisIndex) {
        this.id = axisIndex;
        this.axisIndex = axisIndex;
    }

    public ParallelCoordinatesAxis(int axisIndex, NumberAxis axis, String label, HBox labelBox, RangeSlider filterSlider, Button btnInvert, Button btnLeft, Button btnRight) {
        this(axisIndex);
        initialize(axis, label, labelBox, filterSlider, btnInvert, btnLeft, btnRight);
    }

    /**
     * supports the basic constructor by setting the values AFTER already creating the axis
     * for now, this is the approach which is used when calling bindAxes in the chart
     * TODO moveAxes: not sure if this is needed or can be replaced when refactoring for performance
     *
     * @param axis
     * @param label
     * @param labelBox
     * @param filterSlider
     * @param btnInvert
     * @param btnLeft
     * @param btnRight
     */
    public void initialize(NumberAxis axis, String label, HBox labelBox, RangeSlider filterSlider, Button btnInvert, Button btnLeft, Button btnRight) {
        this.axis = axis;
        this.label = label;
        this.labelBox = labelBox;
        this.filterSlider = filterSlider;

        if (filterSlider != null) {
            filterHigh = filterSlider.getHighValue();
            filterLow = filterSlider.getLowValue();
        }

        this.btnInvert = btnInvert;
        this.btnLeft = btnLeft;
        this.btnRight = btnRight;

        setTickLabelFormatter();
    }

    public void registerDragAndDropListener(ParallelCoordinatesChart chart, ParallelCoordinatesChart.AxisSeparatorLabel labelDragAndDrop) {
        filterSlider.setOnDragDetected(event -> {
            /* drag was detected, start a drag-and-drop gesture*/
            /* allow any transfer mode */
            Dragboard db = axis.startDragAndDrop(TransferMode.MOVE);

            /* Put a string on a dragboard */
            ClipboardContent content = new ClipboardContent();
            content.putString(axisIndex + "");
            db.setContent(content);
            System.out.println("drag started from:" + axisIndex);

            highlightAxis(true);
            event.consume();
        });

        // this is needed to register which transfer modes are allowed
        filterSlider.setOnDragOver(event -> {
            /* data is dragged over the target */
            /* if it has a string data */
            if (event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });


        filterSlider.setOnDragEntered(event -> {
            axis.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        filterSlider.setOnDragExited(event -> {
            String oldAxisAsString = event.getDragboard().getString();
            if (event.getDragboard().hasString() && oldAxisAsString != null) {
                System.out.println("drag dropped at:" + axisIndex + " from " + oldAxisAsString);

                int oldAxisIndex = Integer.parseInt(oldAxisAsString);
                if (oldAxisIndex != axisIndex) {
                    chart.getAxisByIndex(oldAxisIndex).highlightAxis(false);
                }
            }

            event.consume();
        });

        filterSlider.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            boolean success = false;
            String oldAxisAsString = event.getDragboard().getString();
            if (event.getDragboard().hasString() && oldAxisAsString != null) {
                success = true;
                System.out.println("drag dropped at:" + axisIndex + " from " + oldAxisAsString);

                int oldAxisIndex = Integer.parseInt(oldAxisAsString);
                if (oldAxisIndex != axisIndex) {
                    chart.swapAxes(oldAxisIndex, axisIndex);
                }

                chart.getAxisByIndex(oldAxisIndex).highlightAxis(false);
            }
            highlightAxis(false);

            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);
            event.consume();
        });

        labelDragAndDrop.setOnDragOver(event -> {
            /* data is dragged over the target */
            /* if it has a string data */
            if (event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        labelDragAndDrop.setOnDragEntered(event -> {
            labelDragAndDrop.show(true);
        });

        labelDragAndDrop.setOnDragExited(event -> {
            labelDragAndDrop.show(false);
        });


        labelDragAndDrop.setOnDragDropped(event -> {
          /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            boolean success = false;
            String oldAxisAsString = event.getDragboard().getString();
            if (event.getDragboard().hasString() && oldAxisAsString != null) {
                success = true;
                //System.out.println("drag dropped between:" + labelDragAndDrop.getAxisLeft() + " and " + labelDragAndDrop.getAxisRight());

                int oldIndex = Integer.parseInt(oldAxisAsString);

                ParallelCoordinatesAxis axisRight = labelDragAndDrop.getAxisRight();
                int newIndex;
                if (axisRight != null) {
                    newIndex = axisRight.getAxisIndex();
                    if (oldIndex < newIndex) {
                        newIndex--;
                    }
                } else {
                    newIndex = chart.getAxisByIndex(chart.getAttributeCount() - 1).getAxisIndex();
                }

                if (newIndex != oldIndex) {
                    chart.moveAxis(oldIndex, newIndex);
                }

                chart.getAxisByIndex(oldIndex).highlightAxis(false);
                labelDragAndDrop.show(false);
            }


            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);
            event.consume();
        });
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

        if (filterSlider != null) {

            // adjust filters
            double filterHighTmp = 1.0 - filterLow;
            double filterLowTmp = 1.0 - filterHigh;

            if (!inverted) {
                //when inverting back to normal, change values
                filterHighTmp = filterHigh;
                filterLowTmp = filterLow;
            }

            // remove listeners and add them again afterwards
            ChangeListener<Number> highListener = (ChangeListener<Number>) filterSlider.getProperties().get("highListener");
            ChangeListener<Number> lowListener = (ChangeListener<Number>) filterSlider.getProperties().get("lowListener");
            filterSlider.highValueProperty().removeListener(highListener);
            filterSlider.lowValueProperty().removeListener(lowListener);

            if (!inverted) {
                filterLow = filterLowTmp;
                filterHigh = filterHighTmp;
            }
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

    public Button getBtnInvert() {
        return btnInvert;
    }

    public void setBtnInvert(Button btnInvert) {
        this.btnInvert = btnInvert;
    }

    public int getId() {
        return id;
    }

    public Button getBtnLeft() {
        return btnLeft;
    }

    public void setBtnLeft(Button btnLeft) {
        this.btnLeft = btnLeft;
    }

    public Button getBtnRight() {
        return btnRight;
    }

    public void setBtnRight(Button btnRight) {
        this.btnRight = btnRight;
    }

    /**
     * Moves this axes and ALL its components to a new position
     * TODO moveAxes: in the future, this should be replaced by something better performing (or something doing more)
     */
    public void moveToPosition(int newPos, Map<Integer, ParallelCoordinatesAxis> axes/*DoubleBinding axisSeparation*/) {
        // todo finish
        // for now, only reset index!
        // because afterwards, we attempt a full redraw anyway
        // for the future, only reset btns and numberaxis X position here
        setAxisIndex(newPos);
    }

    public void highlightAxis(boolean axisHighlighted) {
        Background background = null;

        if (axisHighlighted) {
            background = new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY));
        }

        // has to be done this way, not via opacity!
        axis.setBackground(background);
    }
}
