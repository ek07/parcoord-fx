package tugraz.ivis.parcoord.chart;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
    private double top;
    private double left;

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

        //   NumberAxis nx = new NumberAxis(0, 10, 1);
        // nx.setSide(Side.LEFT);
        //nx.minHeightProperty().bind(heightProperty());
        //nx.prefHeightProperty().bind(heightProperty());
        //getChartChildren().add(nx);
    }

    /**
     * Binds the set data to the graph
     */
    public void bindData() {
        //drawBorder();
        //drawAxes();
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

    // TODO: thomas bind axes
    private void drawAxes() {
        double axisSeparation = getAxisSeparation();
        int numAxes = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            Path path = new Path();
            double xPos = axisSeparation + axisSeparation * iAxis; // dont start completely at edge

            MoveTo moveTo = new MoveTo();
            moveTo.setX(xPos);
            moveTo.setY(0);
            path.getElements().add(moveTo);

            LineTo lineTo = new LineTo();
            lineTo.setX(xPos);
            lineTo.setY(innerHeightProperty.doubleValue());
            path.getElements().add(lineTo);

            getChartChildren().add(path);
            //System.out.println("xPos" + xPos + " height" + height);
            // TODO labeling
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

    // TODO: probably not needed later
    private double getAxisSeparation() {
        return (innerWidthProperty.doubleValue() / ((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
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
    }

}