package simpledb.execution;

import simpledb.common.DbException;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.NoSuchElementException;

/**
 * Filter is an operator that implements a relational select.
 */
public class Filter extends Operator {

    private static final long serialVersionUID = 1L;
    private Predicate p;
    private OpIterator child;
    /**
     * Constructor accepts a predicate to apply and a child operator to read
     * tuples to filter from.
     *
     * @param p     The predicate to filter tuples with
     * @param child The child operator
     */
    public Filter(Predicate p, OpIterator child) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:07:43
        this.p = p;
        this.child = child;
    }

    public Predicate getPredicate() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:07:54
        return this.p;
    }

    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:08:03
        return child.getTupleDesc();
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:18:38
        super.open();
        this.child.open();
    }

    public void close() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:19:13
        super.close();
        this.child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:19:41
        this.child.rewind();
    }

    /**
     * AbstractDbIterator.readNext implementation. Iterates over tuples from the
     * child operator, applying the predicate to them and returning those that
     * pass the predicate (i.e. for which the Predicate.filter() returns true.)
     *
     * @return The next tuple that passes the filter, or null if there are no
     *         more tuples
     * @see Predicate#filter
     */
    protected Tuple fetchNext() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:26:55
        while (this.child.hasNext()) {
            Tuple t = this.child.next();
            if (this.p.filter(t)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public OpIterator[] getChildren() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:27:16
        return new OpIterator[]{this.child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:27:54
        this.child = children[0];
    }

}
