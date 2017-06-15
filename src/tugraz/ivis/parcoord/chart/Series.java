package tugraz.ivis.parcoord.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Series which contains list of records
 */
public class Series {

    /**
     * name of the series
     */
    private String name = "?";

    /**
     * List of records in the series
     */
    private ObservableList<Record> records = FXCollections.observableArrayList();

    public Series(String name, List<Record> records) {
        this(records);
        this.name = name;

    }

    public Series(List<Record> records) {
        this.records.addAll(records);
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