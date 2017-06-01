package tugraz.ivis.parcoord.util.importer;

import tugraz.ivis.parcoord.util.data.ChartData;
import tugraz.ivis.parcoord.util.data.DataSet;
import tugraz.ivis.parcoord.util.data.Record;

import java.util.ArrayList;

// basic skeleton for data importers
// see ExampleDataImporter for basic example implementation
public abstract class DataImporter {
    // TODO: this may not be needed, provide it here for now
    // only import columns given the specific indices
    protected int[] selectedColumnIndices;

    public DataImporter() {
    }

    public DataImporter(int[] selectedColumnIndices) {
        this.selectedColumnIndices = selectedColumnIndices;
    }

    // matches labels to data
    public DataSet getDataSet() {
        return new DataSet(getColumnLabels(), getRecords());
    }

    public int[] getSelectedColumnIndices() {
        return selectedColumnIndices;
    }

    public void setSelectedColumnIndices(int[] columnIndices) {
        selectedColumnIndices = columnIndices;
    }

    // ALL column labels
    public abstract String[] getColumnLabels();

    // the "raw" data records retrieved from the source without labels, only values
    public abstract Record[] getRecords();

    // TODO implement to match data to axes?
    // or do this later?
    public abstract ArrayList<ChartData> getChartData();

}
