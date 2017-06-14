/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.chart;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mchegini
 */
public abstract class HighDimensionalChart extends Chart implements Brushable {
    protected ObservableList<Series> series = FXCollections.observableArrayList();
    protected double top;
    protected double left;
    protected boolean showLabels = true;

    /**
     * Property holding the height of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     */
    protected DoubleProperty innerHeightProperty = new SimpleDoubleProperty();
    /**
     * Property holding the width of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, etc.)
     */
    protected DoubleProperty innerWidthProperty = new SimpleDoubleProperty();


    public HighDimensionalChart() {
    }

    /**
     * Draws a border around the chart
     */
    protected void drawBorder() {
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


    /**
     * Immediately clears the whole chart and chartChildren
     */
    public void clear() {
        getChartChildren().clear();
        series.clear();
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
        resizeAxes();
    }

    @Override
    public void redrawSelected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawAllBackground() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSelectItems(ObservableList<Record> records) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSelectItems(ObservableList<Record> records) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Adds a series to the given Chart
     * Inspired by: http://docs.oracle.com/javafx/2/charts/scatter-chart.htm
     *
     * @param s the series to add to this Chart
     */
    public void addSeries(Series s) {
        boolean firstDraw = series.isEmpty();
        series.add(s);

        if (firstDraw) {
            bindAxes();
        }

        bindSeries(s);
    }

    /**
     * Removes Series from the list of all series in this Chart
     * TODO: test
     *
     * @param index of series to remove
     */
    public void removeSeries(int index) {
        removeSeries(series.get(index));
    }


    /**
     * Removes Series from the list of all series in this Chart
     * TODO: test
     *
     * @param s the series to be removed
     */
    public void removeSeries(Series s) {
        for (Record r : s.getRecords()) {
            getChartChildren().removeAll(r.getPath());
        }
        series.remove(s);
    }

    /**
     * remove all Series from the graph
     * TODO: test
     */
    public void clearSeries() {
        List<Path> paths = new ArrayList<>();
        for (Series s : series) {
            for (Record r : s.getRecords()) {
                getChartChildren().removeAll(r.getPath());
            }
        }
        series.clear();
    }

    /**
     * Subclasses should implement this method to bind axes to the chart
     */
    protected abstract void bindAxes();

    /**
     * Subclasses should implement this method to resize axes
     */
    protected abstract void resizeAxes();

    /**
     * Subclasses should implement this method to bind a given series to the chart
     * also, the paths of the records should be set accordingly
     */
    protected abstract void bindSeries(Series s);
}
