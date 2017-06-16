package tugraz.ivis.parcoord.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

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
     * The color of the series (BLACK default)
     */
    private Color color = Color.BLACK;
    
    /**
     * The opacity for the series (0.2 default)
     */
    private double opacity = 0.2;

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
        for(Record record : this.records) {
        	record.setSeries(this);
        }
    }
    
    public Series(String name, List<Record> records, Color color, double opacity) {
        this(name, records);
        this.color = color;
        this.opacity = opacity;

    }

    public Series(List<Record> records, Color color, double opacity) {
        this(records);
        this.color = color;
        this.opacity = opacity;
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

//    public void setRecords(ObservableList<Record> records) {
//        this.records = records;
//    }

    public int getSeriesSize() {
        return records.size();
    }

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getOpacity() {
		return opacity;
	}

	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}
    
}