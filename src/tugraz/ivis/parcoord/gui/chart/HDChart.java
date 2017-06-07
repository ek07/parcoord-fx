/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.gui.chart;

import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import tugraz.ivis.parcoord.util.importer.Brushable;
import tugraz.ivis.parcoord.util.importer.DataModel;
import tugraz.ivis.parcoord.util.importer.Item;

/**
 *
 * @author mchegini
 */
public class HDChart extends Chart implements Brushable {

    private DataModel dataModel;

    public HDChart(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSelectItems(ObservableList<Item> items) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSelectItems(ObservableList<Item> items) {
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
