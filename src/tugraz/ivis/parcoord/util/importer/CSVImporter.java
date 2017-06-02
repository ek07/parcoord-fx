package tugraz.ivis.parcoord.util.importer;

import java.io.File;
import tugraz.ivis.parcoord.util.data.ChartData;
import tugraz.ivis.parcoord.util.data.Record;

import java.util.ArrayList;
import tugraz.ivis.parcoord.util.data.DataModel;

public class CSVImporter extends DataImporter {

    private DataModel model;

    public CSVImporter(File file) {
        
    }

    @Override
    public String[] getColumnLabels() {
        // TODO
        return null;
    }

    @Override
    public Record[] getRecords() {
        // TODO
        return null;
    }

    @Override
    public ArrayList<ChartData> getChartData() {
        // TODO
        return null;
    }
}
