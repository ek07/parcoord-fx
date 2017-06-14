package tugraz.ivis.parcoord.chart;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
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
import java.util.List;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private List<String> axesLabels = new ArrayList<>();
    private ArrayList<ParallelCoordinatesAxis> axes = new ArrayList<>();

    /**
     * Default constructor needed for FXML
     */
    public ParallelCoordinatesChart() {
    }

    /**
     * Sets the new axesLabels
     *
     * @param axesLabels the labels of the axes
     */
    public void setAxisLabels(List<String> axesLabels) {
        this.axesLabels = axesLabels;
    }

    /**
     * Immediately clears the whole chart and chartChildren
     */
    @Override
    public void clear() {
        super.clear();
        axesLabels.clear();
        axes.clear();
    }

    /**
     * creates the axes and binds them to the Chart
     */
    protected void bindAxes() {
        int numAxes = getAttributeCount();

        // configurable
        double spaceBetweenTicks = 50;
        double labelMinWidth = 500;
        double labelYOffset = 50;

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {

            String label = null;
            if (axesLabels.size() - 1 >= iAxis) {
                if (iAxis < axesLabels.size()) {
                    label = axesLabels.get(iAxis);
                } else {
                    label = "?";
                }
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

    /**
     * resizes the axes
     * TODO: can't this be binded?
     */
    protected void resizeAxes() {
        for (ParallelCoordinatesAxis axis : axes) {
            axis.getAxis().resize(1.0, innerHeightProperty.doubleValue());
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
        return innerWidthProperty().divide(getAttributeCount() + 1);
    }

    /**
     * TODO: is this the best way to get attribute/axis count
     */
    private int getAttributeCount() {
        int valueCount = 0;
        if (series.size() > 0) {
            if (series.get(0).getRecords().size() > 0) {
                valueCount = series.get(0).getRecord(0).getValues().size();
            }
        }

        return valueCount;
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        super.layoutChartChildren(top, left, width, height);
        resizeAxes();
    }

    @Override
    protected void bindSeries(Series s) {
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        DoubleProperty heightProp = innerHeightProperty();

        int numRecords = s.getRecords().size();
        int numColumns = getAttributeCount();
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (Record record : s.getRecords()) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.xProperty().bind(axisSeparation);
            moveTo.yProperty().bind(heightProp.divide(2.0).subtract(top));

            path.getElements().add(moveTo);
            for (int column = 0; column < numColumns; column++) {
                Object dataPoint = record.getAttByIndex(column);

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
            record.setPath(path);
            getChartChildren().add(path);
        }
    }
}