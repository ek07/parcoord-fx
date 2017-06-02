/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.util.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileModel stores the information in a .CSV file (High dimensional data set)
 *
 * @author Lin Shao
 */
public class DataModel {

    /**
     * what is the delimiter
     */
    private String delimiter;

    /**
     * number of attributes in the header
     */
    private int nrOfAttributes;
    /**
     * Header of data
     */
    private ArrayList<String> dataHeader;

    /**
     * Whole data set is stored in this Array of ArrayList. to access each
     * dimension do dataSet[n] and to access a point do a for on all dimensions
     * dataSet should have categories at the end
     */
    private ArrayList<Object>[] dataSet;

    /**
     * Min and Max value of every dimension. First value in Double[] is min and
     * second is max
     */
    private ArrayList<Double[]> minMaxValues;
    private ArrayList<Integer> classIndex;

    /**
     * Number of different categories (for example a data set can have 2
     * categories and each category includes 3 clusters)
     */
    private int nrOfCatDim;
    private ArrayList<String> classNames = new ArrayList<>();

    /**
     * Constructor for creating a new DataModel, e.g.
     * new DataModel(file.getAbsolutePath(), ";", true);
     *
     * @param path the file path
     * @param delimiter e.g. ; or :
     * @param normalize whether normalize (0-1) or not the points
     */
    public DataModel(String path, String delimiter, boolean normalize) {

        try {
            this.delimiter = delimiter;

            BufferedReader in = new BufferedReader(new FileReader(path));
            readHeader(in.readLine());
            readData(in);
            if (normalize) {
                dataSet = normalizeData(dataSet);
            }

        } catch (IOException ex) {
            Logger.getLogger(DataModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Read header of the file
     *
     * @param in
     */
    public void readHeader(String in) {
        String[] header = in.split(delimiter);
        nrOfAttributes = header.length;

        dataHeader = new ArrayList<>();
        dataSet = new ArrayList[nrOfAttributes];
        classIndex = new ArrayList<>();

        for (int i = 0; i < header.length; i++) {
            if (header[i].contains("Class") || header[i].contains("class") || header[i].contains("(c)") || header[i].contains("(C)")) {
                nrOfCatDim++;
                classIndex.add(i);
            }
            this.dataHeader.add(header[i]);
            this.dataSet[i] = new ArrayList<>();
        }
    }

    /**
     * read data from the file and store it in dataSet
     *
     * @param in
     */
    private void readData(BufferedReader in) {
        try {
            String next;
            String values[];

            minMaxValues = new ArrayList<>();

            /**
             * initialize minMaxValues
             */
            for (int i = 0; i < dataHeader.size(); i++) {
                Double[] minMax = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
                minMaxValues.add(minMax);
            }

            while ((next = in.readLine()) != null) {
                values = next.split(delimiter);
                for (int i = 0; i < values.length; i++) {
                    if (values[i].length() > 0 && !values[i].contains("?")) {    // && values[i].matches("([0-9]*)\\\\.[0]")
                        if (isNumeric(values[i])) {
                            double value = Double.parseDouble(values[i]);
                            dataSet[i].add(value);
                            minMaxValues.get(i)[0] = (minMaxValues.get(i)[0] > value) ? value : minMaxValues.get(i)[0];     //min
                            minMaxValues.get(i)[1] = (minMaxValues.get(i)[1] < value) ? value : minMaxValues.get(i)[1];     //max
                        } else {
                            dataSet[i].add(values[i]);
                        }
                    } //for iris data set no class information!
                    else {
                        dataSet[i].add(null);
                    }
                }
                if (values.length < dataHeader.size()) {
                    for (int e = values.length; e < dataHeader.size(); e++) {
                        dataSet[e].add(null);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DataModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Normalize the points in data set (scale all the points to 0-1)
     *
     * @param data
     * @return
     */
    private ArrayList<Object>[] normalizeData(ArrayList<Object>[] data) {
        ArrayList<Object>[] normalizedData = new ArrayList[data.length];

        for (int i = 0; i < data.length; i++) {
            normalizedData[i] = new ArrayList<Object>();
            for (int j = 0; j < data[i].size(); j++) {

                if (data[i].get(j) != null) {
                    if (isNumeric(data[i].get(j).toString())) {
                        double val = (((Double) data[i].get(j)) - minMaxValues.get(i)[0]) / (minMaxValues.get(i)[1] - minMaxValues.get(i)[0]);
                        normalizedData[i].add(val);
                    } else {
                        // string
                        normalizedData[i].add(data[i].get(j));
                    }
                } else {
                    // null
                    normalizedData[i].add(data[i].get(j));
                }
            }
        }
        return normalizedData;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public int getNrOfAttributes() {
        return nrOfAttributes;
    }

    public int getNrOfCatDims() {
        return nrOfCatDim;
    }

    public ArrayList<Integer> getClassIndex() {
        return classIndex;
    }

    public ArrayList<String> getDataHeader() {
        return dataHeader;
    }

    public String getHeaderAt(int index) {
        return dataHeader.get(index);
    }

    public int getHeaderIndex(String name) {
        for (int i = 0; i < dataHeader.size(); i++) {
            if (name.equals(dataHeader.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Object>[] getDataSet() {
        return dataSet;
    }

    public ArrayList<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(ArrayList<String> classNames) {
        this.classNames = classNames;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public void printHeader() {
        System.out.print("Header: ");
        getDataHeader().stream().forEach(header -> {
            System.out.print(header + " ");
        });
    }

    public void printDataSet() {
        System.out.print("Data Set: ");

        int size = getDataSet().length;
        for (int i = 0; i < size; i++) {
            {
                getDataSet()[i].stream().forEach(data -> {
                    System.out.print(data + "   ");
                });
            }
            System.out.println();
        }
    }

    /**
     * get a node by it's index, including categories
     *
     * @param index index of the node
     * @return ArrayList of attributes
     */
    public ArrayList<Object> getNodeByIndex(int index) {

        int size = getDataSet().length;

        ArrayList<Object> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(getDataSet()[i].get(index));
        }

        return result;
    }

    /**
     * get a node by it's index, without categories
     *
     * @param index
     * @return
     */
    public ArrayList<Object> getNodeWithoutCategory(int index) {

        ArrayList<Object> result = new ArrayList<>();
        for (int i = 0; i < getNumberAttributes(); i++) {
            result.add(getDataSet()[i].get(index));
        }

        return result;
    }

    /**
     * @return number of dataSet attributes (without category dimension)
     */
    public int getNumberAttributes() {
        return this.getDataHeader().size() - this.getNrOfCatDims();
    }
}
