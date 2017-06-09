/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.chart;

import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;

/**
 * @author mchegini
 */
public class HighDimensionalChart extends Chart implements Brushable {
    public HighDimensionalChart() {
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
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

    @Override
    public void redrawSelected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawAllBackground() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
