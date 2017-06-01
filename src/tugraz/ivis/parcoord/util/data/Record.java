package tugraz.ivis.parcoord.util.data;

/**
 * Created by thorsten on 6/1/17.
 */
public class Record {
    Object data[];

    public Record(Object[] data) {
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }
}
