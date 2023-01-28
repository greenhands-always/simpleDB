package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         */
        public final Type fieldType;

        /**
         * The name of the field
         */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        @Override
        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    private ArrayList<TDItem> tdItems = new ArrayList<>();

    /**
     * @return An iterator which iterates over all the field TDItems
     * that are included in this TupleDesc
     */
    public Iterator<TDItem> iterator() {
        return this.tdItems.iterator();
        // ? if tdItemLength == 0, NullPointerException may be thrown
        // Done by Huangyihang in 2023-01-27 23:15:26
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr  array specifying the number of and types of fields in this
     *                TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may
     *                be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        for (int i = 0; i < typeAr.length; i++) {
            this.tdItems.add(new TDItem(typeAr[i], fieldAr[i]));
        }
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 18:01:59
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr array specifying the number of and types of fields in this
     *               TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        for (Type type : typeAr) {
            this.tdItems.add(new TDItem(type, "unnamed"));
        }
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 23:02:18
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 23:04:22
        return this.tdItems.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 23:09:30
        if (i <= 0 || i > this.tdItems.size()) {
            throw new NoSuchElementException();
        } else {
            return this.tdItems.get(i).fieldName;
        }
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid
     *          index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 23:09:56
        if (i <= 0 || i > this.tdItems.size()) {
            throw new NoSuchElementException();
        } else {
            return this.tdItems.get(i).fieldType;
        }
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int indexForFieldName(String name) throws NoSuchElementException {
        // TODO: some code goes here
        for (int i = 0; i < this.tdItems.size(); i++) {
            if (this.tdItems.get(i).fieldName.equals(name)) {
                return i;
            }
        }
        // no index until end means NoSuchElementException
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     * Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-27 23:42:34
        int size = 0;
        for (TDItem tdItem : tdItems) {
            size += tdItem.fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1 The TupleDesc with the first fields of the new TupleDesc
     * @param td2 The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-28 10:53
        TupleDesc newTupleDesc = new TupleDesc(null, null);
        newTupleDesc.tdItems.addAll(td1.tdItems);
        newTupleDesc.tdItems.addAll(td2.tdItems);
        return null;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: some code goes here
        TupleDesc tdObj;
        if (o instanceof TupleDesc) {
            tdObj = (TupleDesc) o;
        } else {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (tdObj.numFields() == this.numFields()) {
            for (int i = 0; i < tdObj.numFields(); i++) {
                if (!tdObj.tdItems.get(i).fieldType.equals(this.tdItems.get(i).fieldType)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        // TODO: some code goes here
        StringBuilder sb = new StringBuilder();
        for (TDItem tdItem : this.tdItems) {
            sb.append(tdItem.toString());
        }
        return sb.toString();
    }
}
