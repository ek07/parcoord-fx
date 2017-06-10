package tugraz.ivis.parcoord.chart;


import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;

import org.controlsfx.control.RangeSlider;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private ArrayList<Object>[] data;// => list of columns
    private ArrayList<String> axisLabels;
    private ArrayList<ParallelCoordinatesAxis> axes = new ArrayList<ParallelCoordinatesAxis>();
    private double top;
    private double left;
    private double width;
    private double height;
    private boolean showLabels = true;

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

    // binds the set data to the graph
    public void bindData() {
        //drawBorder();
    	
        bindAxes();
        bindRecords();
        System.out.println("heightProperty" + heightProperty().doubleValue() + "|height:" + height + "|innerheight:" + innerHeightProperty());
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
        lineTo.setX(width);
        lineTo.setY(0);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(width);
        lineTo.setY(height);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.setY(height);
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
            
            // axis
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
            
            // filters
            RangeSlider vSlider = new RangeSlider(lowerBound, upperBound, lowerBound, upperBound);
            vSlider.setOrientation(Orientation.VERTICAL);
            vSlider.setShowTickLabels(false);
            vSlider.setShowTickMarks(false);
            vSlider.translateXProperty().bind(trueAxisSeparation);
            
        	getChartChildren().add(numberAxis);
        	getChartChildren().add(box);
        	getChartChildren().add(vSlider);
        	
        	// have to style after adding it (CSS wouldn't be accessible otherwise)
            vSlider.applyCss();
            vSlider.lookup(".range-slider .track").setStyle("-fx-opacity: 0;");
            vSlider.lookup(".range-slider .range-bar").setStyle("-fx-opacity: 0.15;");
        	
            ParallelCoordinatesAxis pcAxis = new ParallelCoordinatesAxis(numberAxis, iAxis, label, box, vSlider);
        	axes.add(pcAxis);
        }
        resizeAxes();
    }
    
    private void resizeAxes() {
    	for(ParallelCoordinatesAxis axis : axes) {
    		axis.getAxis().resize(1.0, height);
    		axis.getFilterSlider().resize(1.0, height);
    	}
    }

    private void bindRecords() {
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        ReadOnlyDoubleProperty heightProp = heightProperty();
        System.out.println("heightProperty" + heightProperty().doubleValue() + "|height:" + height + "|innerheight:" + innerHeightProperty());
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

    // getHeightProperty with outer values (title height, etc.) subtracted
    // TODO thorsten: doesn't work right now, try to fix!
    public DoubleBinding innerHeightProperty() {
        return heightProperty().subtract(heightProperty().subtract(height));
    }

    // TODO remove when bind finished
    private void drawRecords() {
        double axisSeparation = getAxisSeparation();
        int numRecords = data[0].size();
        int numColumns = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (int record = 0; record < numRecords; record++) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.setX(axisSeparation); // dont start completely at edge
            moveTo.setY(height / 2 - top);

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
                    lineTo.setX(axisSeparation + axisSeparation * column);
                    lineTo.setY(height - (height * value));
                    path.getElements().add(lineTo);
                }
            }
            path.setStroke(new Color(0, 0, 0, 0.2));
            getChartChildren().add(path);
        }
    }

    private DoubleBinding getAxisSeparationBinding() {
        return widthProperty().divide(((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
    }

    private double getAxisSeparation() {
        return (width / ((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
    }

    // TODO replace redraw with binding
    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        //System.out.println("LayoutChartChildren called");
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        if (data == null || axisLabels == null || data.length != axisLabels.size()) {
            System.out.println("Data doesnt add up");
            return;
        }
        resizeAxes();
    }
}
