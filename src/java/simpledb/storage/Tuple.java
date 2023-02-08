package simpledb.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {
    private static final long serialVersionUID = 1L;
    private TupleDesc tupleDesc;
    private RecordId recordID;
    private Field[] fields;

    /**
     * Create a new tuple with the specified schema (type).
     *
     * @param td the schema of this tuple. It must be a valid TupleDesc
     *           instance with at least one field.
     */
    public Tuple(TupleDesc td) {
        this.tupleDesc = td;
        this.fields = new Field[td.numFields()];
        // TODO: some code goes here
        //Done by Huangyihang in 2023-01-28 11:42:55

    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 11:44:45
        return this.tupleDesc;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     * be null.
     */
    public RecordId getRecordId() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 11:45:06
        return this.recordID;
    }

    /**
     * Set the RecordId information for this tuple.
     *
     * @param rid the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // TODO: some code goes here

        this.recordID = rid;
    }

    /**
     * Change the value of the ith field of this tuple.
     *
     * @param i index of the field to change. It must be a valid index.
     * @param f new value for the field.
     */
    public void setField(int i, Field f) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 11:57:17
        this.fields[i] = f;
    }

    /**
     * @param i field index to return. Must be a valid index.
     * @return the value of the ith field, or null if it has not been set.
     */
    public Field getField(int i) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 12:02:22
        return this.fields[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     * <p>
     * column1\tcolumn2\tcolumn3\t...\tcolumnN
     * <p>
     * where \t is any whitespace (except a newline)
     */
    public String toString() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 16:29:35
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<this.fields.length;i++){
            sb.append(this.fields[i].toString()+" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * @return An iterator which iterates over all the fields of this tuple
     */
    public Iterator<Field> fields() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 16:30:33
        return (Iterator<Field>) Arrays.asList(fields).iterator();
    }

    /**
     * reset the TupleDesc of this tuple (only affecting the TupleDesc)
     */
    public void resetTupleDesc(TupleDesc td) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 16:31:34
        this.tupleDesc = td;
    }
}
