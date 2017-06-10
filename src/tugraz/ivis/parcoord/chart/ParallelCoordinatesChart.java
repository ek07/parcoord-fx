package tugraz.ivis.parcoord.chart;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private ArrayList<Object>[] data;// => list of columns
    private ArrayList<String> axisLabels;
    private ArrayList<ParallelCoordinatesAxis> axes = new ArrayList<ParallelCoordinatesAxis>();
    private double top;
    private double left;
    private boolean showLabels = true;

    /**
     * Property holding the height of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     */
    private DoubleProperty innerHeightProperty = new SimpleDoubleProperty();
    /**
     * Property holding the width of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, etc.)
     */
    private DoubleProperty innerWidthProperty = new SimpleDoubleProperty();

    /**
     * Default constructor needed for FXML
     */
    public ParallelCoordinatesChart() {
    }

    public void updateData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        setData(data, axisLabels);
        bindData();
    }

    public void setData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        this.data = data;
        this.axisLabels = axisLabels;
        System.out.println("imported records: " + data[0].size() + " with columns:" + data.length);
    }

    /**
     * Binds the set data to the graph
     */
    public void bindData() {
        //drawBorder();

        bindAxes();
        bindRecords();
    }

    public void forceRedraw() {
        getChartChildren().clear();
        bindData();
    }

    // TODO: not needed for now
    private void drawBorder() {
        Path path = new Path();

        MoveTo moveTo = new MoveTo();
        moveTo.setX(0);
        moveTo.setY(0);
        path.getElements().add(moveTo);

        LineTo lineTo = new LineTo();
        lineTo.setX(innerWidthProperty.doubleValue());
        lineTo.setY(0);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(innerWidthProperty.doubleValue());
        lineTo.setY(innerHeightProperty.doubleValue());
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.setY(innerHeightProperty.doubleValue());
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.setY(0);
        path.getElements().add(lineTo);

        getChartChildren().add(path);
    }

    private void bindAxes() {
        int numAxes = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel

        // configurable
        double spaceBetweenTicks = 50;
        double labelMinWidth = 500;
        double labelYOffset = 50;

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {

            String label = null;
            if (axisLabels.size() - 1 >= iAxis) {
                label = axisLabels.get(iAxis);
            }

            DoubleBinding trueAxisSeparation = getAxisSeparationBinding().multiply(iAxis + 1);

            // TODO get real bounds
            double upperBound = 10.0;
            double lowerBound = 0.0;
            double delta = Math.abs(upperBound - lowerBound);
            NumberAxis numberAxis = new NumberAxis(null, lowerBound, upperBound, 1.0);

            numberAxis.setSide(Side.LEFT);
            numberAxis.setMinorTickVisible(false);
            numberAxis.setAnimated(false);
            numberAxis.translateXProperty().bind(trueAxisSeparation);
            numberAxis.tickUnitProperty().bind(heightProperty().divide(heightProperty()).divide(heightProperty()).multiply(spaceBetweenTicks).multiply(delta));

            // label
            Label labelNode = new Label(label);
            labelNode.setMinWidth(labelMinWidth);
            labelNode.setAlignment(Pos.CENTER);
            HBox box = new HBox(labelNode);
            box.translateXProperty().bind(trueAxisSeparation.subtract(labelMinWidth / 2));
            box.translateYProperty().bind(heightProperty().subtract(labelYOffset));

            box.requestLayout();
            box.layout();

            getChartChildren().add(numberAxis);
            getChartChildren().add(box);

            ParallelCoordinatesAxis pcAxis = new ParallelCoordinatesAxis(numberAxis, iAxis, label);
            axes.add(pcAxis);
        }
        resizeAxes();
    }
    
    private void resizeAxes() {
        for (ParallelCoordinatesAxis axis : axes) {
            axis.getAxis().resize(1.0, innerHeightProperty.doubleValue());
        }
    }


    /**
     * Binds the dataset to the chart.
     * <p>
     * TODO: why are there vertical lines at the beginning of the graph??
     */
    private void bindRecords() {
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        DoubleProperty heightProp = innerHeightProperty();

        int numRecords = data[0].size();
        int numColumns = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (int record = 0; record < numRecords; record++) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.xProperty().bind(axisSeparation);
            moveTo.yProperty().bind(heightProp.divide(2.0).subtract(top));

            path.getElements().add(moveTo);
            for (int column = 0; column < numColumns; column++) {
                Object dataPoint = data[column].get(record);

                if (dataPoint instanceof String) {
                    break;
                }

                Double value = (Double) dataPoint;
                //System.out.println("data at " + record + ", col:" + column + ";" + "dataPoint" + value);
                if (value != null) {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(axisSeparation.add(axisSeparation.multiply(column)));
                    lineTo.yProperty().bind(heightProp.subtract(heightProp.multiply(value)));
                    path.getElements().add(lineTo);
                }
            }
            path.setStroke(new Color(0, 0, 0, 0.2));
            getChartChildren().add(path);
        }
    }

    /**
     * Returns a property holding the height of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     *
     * @returns property representing the height of the chartContent
     */
    public DoubleProperty innerHeightProperty() {
        return innerHeightProperty;
    }

    /**
     * Returns a property holding the width of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     *
     * @returns property representing the width of the chartContent
     */
    public DoubleProperty innerWidthProperty() {
        return innerWidthProperty;
    }

    /**
     * Returns a DoubleBinding representing the horizontal space between the axes
     * Uses a binding on innerWidthProperty and the data length
     *
     * @returns DoubleBinding which equals the horizontal space between axes
     */
    private DoubleBinding getAxisSeparationBinding() {
        return innerWidthProperty().divide(((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
    }

    /**
     * Overwritten function of the Chart superclass.
     * Automatically called by JavaFX after initialization and resizing of the chart.
     * Used to update the top and left coordinates as well as the innerWidthProperty and innerHeightProperty with the
     * updated values (after resizing the window).
     */
    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        this.top = top;
        this.left = left;
        innerWidthProperty.set(width);
        innerHeightProperty.set(height);

        if (data == null || axisLabels == null || data.length != axisLabels.size()) {
            System.out.println("Error with data, number of axisLabels != number of axis");
            return;
        }
        resizeAxes();
    }

}