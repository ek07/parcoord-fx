package tugraz.ivis.parcoord.chart;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Path;

import java.util.List;

/**
 * A single record
 */
public class Record {
	
	public enum Status {
		VISIBLE,	// indicates that this record is visible
		OPAQUE,		// indicates that this record is hidden
		NONE		// indicates that no meaningful status can be applied (should be treated like VISIBLE for drawing)
	}
	
    /**
     * TODO: calculate when adding
     * the path for the record
     */
    private Path path;

    /**
     * index of the record
     */
    private int index = -1;
    
    /**
     * indicates the current status of the record concerning axisFilters
     */
    private Status axisFilterStatus = Status.VISIBLE;
    
    /**
     * indicates the current status of the record. Is NONE by default to show that no brushing has affected the record yet.
     */
    private Status brushingStatus = Status.NONE;

    /**
     * values of the record
     */
    private ObservableList<Object> values = FXCollections.observableArrayList();

    /**
     * categories of the record
     */
    private ObservableList<String> categories = FXCollections.observableArrayList();

    /**
     * simple constructor for a record
     *
     * @param index      unique index of the record
     * @param values     values of the record
     * @param categories categories of the record
     */
    public Record(int index, List<Object> values, List<String> categories) {
        this(index, values);
        this.categories.addAll(categories);
    }

    /**
     * simple constructor for a record
     *
     * @param index  unique index of the record
     * @param values values of the record
     */
    public Record(int index, List<Object> values) {
        this.index = index;
        this.values.addAll(values);
    }
    
    /**
     * Checks whether a record should be visible according to the statuses it has been assigned.
     * 
     * @return true if it should be visible, false otherwise
     */
    public boolean isVisible() {
    	return !(axisFilterStatus == Status.OPAQUE || brushingStatus == Status.OPAQUE);
    }

    public Object getAttByIndex(int index) {
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

    public ObservableList<Object> getValues() {
        return values;
    }

    public void setValues(ObservableList<Object> values) {
        this.values = values;
    }

    public void setCategories(ObservableList<String> categories) {
        this.categories = categories;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

	public Status getAxisFilterStatus() {
		return axisFilterStatus;
	}

	public void setAxisFilterStatus(Status axisFilterStatus) {
		this.axisFilterStatus = axisFilterStatus;
	}

	public Status getBrushingStatus() {
		return brushingStatus;
	}

	public void setBrushingStatus(Status brushingStatus) {
		this.brushingStatus = brushingStatus;
	}
    
    
}
