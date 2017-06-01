package tugraz.ivis.parcoord.util.data;

// NOT USED FOR NOW - let's just work with objects for now (see Record)
public class RecordData<T> {
    private T value;

    public RecordData(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
