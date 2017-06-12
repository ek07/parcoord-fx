/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugraz.ivis.parcoord.chart;

import javafx.collections.FXCollections;
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
    public void redrawSelected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drawAllBackground() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSelectItems(ObservableList<tugraz.ivis.parcoord.chart.Record> records) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addSelectItems(ObservableList<tugraz.ivis.parcoord.chart.Record> records) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // -------------- INNER CLASSES ------------------------------------------------------------------------------------
    /**
     * A single record, we assumed everything is double
     *
     * @author mchegini
     */
    public final static class Record {

        /**
         * index of the record
         */
        private int index = -1;

        /**
         * values of the record
         */
        private ObservableList<Double> values = FXCollections.observableArrayList();

        /**
         * categories of the record
         */
        private ObservableList<String> categories = FXCollections.observableArrayList();

        /**
         * simple constructor for a record
         *
         * @param index unique index of the record
         * @param values values of the record
         * @param categories categories of the record
         */
        public Record(int index, ObservableList<Double> values, ObservableList<String> categories) {
            this.index = index;
            this.values = values;
            this.categories = categories;
        }

        public double getAttByIndex(int index) {
            return values.get(index);
        }

        public String getCatByIndex(int index) {
            return categories.get(index);
        }

        public int getIndex() {
            return index;
        }

        public ObservableList<String> getCategories() {
            return categories;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setValues(ObservableList<Double> values) {
            this.values = values;
        }

        public void setCategories(ObservableList<String> categories) {
            this.categories = categories;
        }

    }

    /**
     * Series which contains set of records
     *
     * @author mchegini
     */
    public static final class Series {

        /**
         * name of the series
         */
        private String name;

        /**
         * List of records in the series
         */
        private ObservableList<HighDimensionalChart.Record> records = FXCollections.observableArrayList();

        public Series(String name, ObservableList<HighDimensionalChart.Record> records) {
            this.name = name;
            this.records = records;
        }

        public Series(ObservableList<HighDimensionalChart.Record> records) {
            this.records = records;
        }

        public int getItemIndex(Record record) {
            return records.indexOf(record);
        }

        public Record getRecord(int index) {
            return records.get(index);
        }

        public String getName() {
            return name;
        }

        public ObservableList<Record> getRecords() {
            return records;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRecords(ObservableList<Record> records) {
            this.records = records;
        }

        public int getSeriesSize() {
            return records.size();
        }
    }
}
