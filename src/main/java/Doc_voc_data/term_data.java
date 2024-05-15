package Doc_voc_data;

public class term_data {
    private int df;
    private long pointer;

    public term_data(int df, long pointer) {
        this.df = df;
        this.pointer = pointer;
    }

    public int getDf() {
        return df;
    }

    public long getPointer() {
        return pointer;
    }
}
