/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.chart;

import javafx.collections.ObservableList;

/**
 * All charts with brushing ability should implement this interface. Later with
 * a list of {@link Brushable}, it is possible to call functions and do brushing across
 * multiple views
 *
 * @author mchegini
 */
public interface Brushable {

    /**
     * remove already selected records from records list
     *
     * @param records records to be deselected
     */
    public void removeSelectItems(ObservableList<Record> records);

    /**
     * add new records to selected and then draw them
     *
     * @param records records to be add to selected
     */
    public void addSelectItems(ObservableList<Record> records);

    /**
     * redraw everything according to selected items
     */
    public void redrawSelected();

    /**
     * draw all the items, usually with a gray neutral color in background for
     * performance
     */
    public void drawAllBackground();

}
