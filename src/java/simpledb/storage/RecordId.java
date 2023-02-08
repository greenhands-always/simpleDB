package simpledb.storage;

import java.io.Serializable;

/**
 * A RecordId is a reference to a specific tuple on a specific page of a
 * specific table.
 */
public class RecordId implements Serializable {

    private static final long serialVersionUID = 1L;
    private PageId pageId;
    private int tupleNumber;
    /**
     * Creates a new RecordId referring to the specified PageId and tuple
     * number.
     *
     * @param pid     the pageid of the page on which the tuple resides
     * @param tupleno the tuple number within the page.
     */
    public RecordId(PageId pid, int tupleno) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 17:54:21
        this.pageId = pid;
        this.tupleNumber = tupleno;
    }

    /**
     * @return the tuple number this RecordId references.
     */
    public int getTupleNumber() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 17:54:43
        return this.tupleNumber;
    }

    /**
     * @return the page id this RecordId references.
     */
    public PageId getPageId() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 17:54:50
        return this.pageId;
    }

    /**
     * Two RecordId objects are considered equal if they represent the same
     * tuple.
     *
     * @return True if this and o represent the same tuple
     */
    @Override
    public boolean equals(Object o) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 17:58:53
        if(this == o) return true;
        if(o == null) return false;
        if(o instanceof RecordId)
            return ((RecordId) o).getPageId().equals(this.pageId) && ((RecordId) o).getTupleNumber() == this.tupleNumber;
        return false;
    }

    /**
     * You should implement the hashCode() so that two equal RecordId instances
     * (with respect to equals()) have the same hashCode().
     *
     * @return An int that is the same for equal RecordId objects.
     */
    @Override
    public int hashCode() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 18:01:23
        String s = pageId.hashCode()+""+tupleNumber;
        return s.hashCode();

    }

}
