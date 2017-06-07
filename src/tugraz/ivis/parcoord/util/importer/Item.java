/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.util.importer;

import java.util.ArrayList;

/**
 * An item is a row in data set
 *
 * @author mchegini
 */
public class Item {

    /**
     * unique index of item
     */
    private int index;

    /**
     * ArrayList of all attributes
     */
    private ArrayList<Object> attributes = new ArrayList<>();

    /**
     * number of attributes
     */
    private int nrOfAttributes;

    /**
     * number of category dimensions
     */
    private int nrOfCatDim;

    public Item(int nrOfAttributes, int nrOfCatDim, ArrayList<Object> attributes) {
        this.nrOfAttributes = nrOfAttributes;
        this.nrOfCatDim = nrOfCatDim;
        this.attributes = attributes;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Object> getAttributes() {
        return attributes;
    }

    public int getNrOfAttributes() {
        return nrOfAttributes;
    }

    public int getNrOfCatDim() {
        return nrOfCatDim;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setAttributes(ArrayList<Object> attributes) {
        this.attributes = attributes;
    }

    public void setNrOfAttributes(int nrOfAttributes) {
        this.nrOfAttributes = nrOfAttributes;
    }

    public void setNrOfCatDim(int nrOfCatDim) {
        this.nrOfCatDim = nrOfCatDim;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < nrOfAttributes; i++) {
            result = result + " " +  this.getAttributes().get(i) ;
        }
        return result;
    }

}
