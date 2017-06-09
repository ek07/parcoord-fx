package tugraz.ivis.parcoord.chart;


import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import tugraz.ivis.parcoord.util.importer.DataModel;

import java.util.ArrayList;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private ArrayList<Object>[] data;// => list of columns
    private ArrayList<String> axisLabels;
    private double top;
    private double left;
    private double width;
    private double height;

    public ParallelCoordinatesChart() {

    }

    // TODO Remove
    public ParallelCoordinatesChart(DataModel dataModel) {
        super(dataModel);
    }

    public void setData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        this.data = data;
        this.axisLabels = axisLabels;
        System.out.println("imported records: " + data[0].size() + " with columns:" + data.length);

        NumberAxis nx = new NumberAxis(0, 10, 1);
        nx.setSide(Side.LEFT);
        nx.minHeightProperty().bind(heightProperty());
        nx.prefHeightProperty().bind(heightProperty());
        getChartChildren().add(nx);
       /* Circle circle = new Circle(30);
        circle.centerXProperty().bind(widthProperty().divide(2));
        circle.centerYProperty().bind(heightProperty().divide(2));
        getChartChildren().add(circle);*/
    }

    public void redraw() {
        //getChartChildren().clear();
        //drawBorder();
        //drawAxes();
        drawRecords();
        //drawRecords(); // for testing 3-time-overlay
        //drawRecords(); // for testing 3-time-overlay
    }

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
            lineTo.setY(height);
            path.getElements().add(lineTo);

            getChartChildren().add(path);
            //System.out.println("xPos" + xPos + " height" + height);
            // TODO labeling
        }
    }


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
            path.setStroke(new Color(0, 0, 0, 0.2git));
            getChartChildren().add(path);
        }
    }

    private double getAxisSeparation() {
        return (width / ((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
    }

    // TODO replace redraw with binding
    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        // TODO: not yet completely sure how/when this will be called
        // for now, only during initialization?
        //System.out.println("LayoutChartChildren called");
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
        if (data == null || axisLabels == null || data.length != axisLabels.size()) {
            System.out.println("Data doesnt add up");
            return;
        }

        redraw();
    }
}
