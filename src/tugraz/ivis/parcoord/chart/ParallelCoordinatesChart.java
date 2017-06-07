package tugraz.ivis.parcoord.chart;


import javafx.scene.chart.Chart;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends Chart {


    private ArrayList<Object>[] data;// => list of columns
    private ArrayList<String> axisLabels;
    private double top;
    private double left;
    private double width;
    private double height;

    public void setData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        this.data = data;
        this.axisLabels = axisLabels;
    }

    public void redraw() {
        getChartChildren().clear();
        drawAxes();
        drawRecords();
    }

    private void drawAxes() {
        double axisSeparation = getAxisSeparation();
        int numAxes = data.length - 1; // TODO: remove later, for now because of Categories in datamodel

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            Path path = new Path();
            double xPos = axisSeparation + axisSeparation * iAxis;

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
        int numColumns = data.length - 1; // TODO: remove later, for now because of Categories in datamodel
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (int record = 0; record < numRecords; record++) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.setX(axisSeparation);
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
            getChartChildren().add(path);
        }
    }

    private double getAxisSeparation() {
        return (width / (data.length + 1));
    }

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
