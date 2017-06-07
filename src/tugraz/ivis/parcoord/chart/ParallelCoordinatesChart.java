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

    public void drawPathsForDataset() {
        double axisSeparation = (width / data.length);
        int numRecords = data[0].size();
        int numColumns = data.length;
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (int record = 0; record < numRecords; record++) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.setX(0);
            moveTo.setY(height / 2 - top);

            path.getElements().add(moveTo);
            for (int column = 0; column < data.length; column++) {
                Object dataPoint = data[column].get(record);

                if (dataPoint instanceof String) {
                    break;
                }

                Double value = (Double) dataPoint;
                //System.out.println("data at " + record + ", col:" + column + ";" + "dataPoint" + value);
                if (value != null) {
                    LineTo lineTo = new LineTo();
                    lineTo.setX(axisSeparation * column);
                    lineTo.setY(height - (height * value));
                    path.getElements().add(lineTo);
                }
            }
            getChartChildren().add(path);
        }
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
        getChartChildren().clear();
        drawPathsForDataset();
    }
}
