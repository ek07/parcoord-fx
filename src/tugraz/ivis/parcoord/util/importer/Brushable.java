/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.util.importer;

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
     * remove already selected items from items list
     *
     * @param items items to be deselected
     */
    public void removeSelectItems(ObservableList<Item> items);

    /**
     * add new items to selected and then draw them
     *
     * @param items items to be add to selected
     */
    public void addSelectItems(ObservableList<Item> items);

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
