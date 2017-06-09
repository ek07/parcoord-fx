/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.chart;

import java.util.ArrayList;

/**
 * An item is a row in data set
 *
 * @author mchegini
 */
// TODO: remove Item and incorporate this into DataModel
public class Record {
    // TODO: think of more complex index handling (e.g. two indices with same values??)
    /**
     * unique index of item
     */
    private int index = -1;

    /**
     * ArrayList of all attributes
     */
    private ArrayList<Object> attributes = new ArrayList<>();

    public Record() {
    }

    public Record(ArrayList<Object> attributes) {
        this.attributes = attributes;
    }

    public Record(int index, ArrayList<Object> attributes) {
        this.index = index;
        this.attributes = attributes;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Object> getAttributes() {
        return attributes;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setAttributes(ArrayList<Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        String result = "";
        int nrOfAttributes = getAttributes().size();
        for (int i = 0; i < nrOfAttributes; i++) {
            result = result + " " + this.getAttributes().get(i);
        }
        return result;
    }

}
