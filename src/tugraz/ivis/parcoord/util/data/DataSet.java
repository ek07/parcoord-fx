package tugraz.ivis.parcoord.util.data;

public class DataSet {
    private String columnLabels[];
    private Record records[];

    public DataSet(String[] columnLabels, Record[] records) {
        this.columnLabels = columnLabels;
        this.records = records;
    }

    public String[] getColumnLabels() {
        return columnLabels;
    }

    public void setColumnLabels(String[] columnLabels) {
        this.columnLabels = columnLabels;
    }

    public Record[] getRecords() {
        return records;
    }

    public void setRecords(Record[] records) {
        this.records = records;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Record rec : records) {
            Object recData[] = rec.getData();
            for (int i = 0; i < columnLabels.length; i++) {
                sb.append(columnLabels[i]).append(": ").append(recData[i]).append("(").append(recData[i].getClass().getSimpleName()).append("); ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
