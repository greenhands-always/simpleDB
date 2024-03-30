package simpledb.execution;

import simpledb.common.DbException;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.NoSuchElementException;

/**
 * The Join operator implements the relational join operation.
 */
public class Join extends Operator {

    private static final long serialVersionUID = 1L;
    private JoinPredicate p;
    private OpIterator child1;
    private OpIterator child2;
    private Tuple temp = null;
    private TupleDesc newTupleDesc = null;

    /**
     * Constructor. Accepts two children to join and the predicate to join them
     * on
     *
     * @param p      The predicate to use to join the children
     * @param child1 Iterator for the left(outer) relation to join
     * @param child2 Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, OpIterator child1, OpIterator child2) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:29:06
        this.p = p;
        this.child1 = child1;
        this.child2 = child2;
        newTupleDesc = TupleDesc.merge(child1.getTupleDesc(), child2.getTupleDesc());
    }

    public JoinPredicate getJoinPredicate() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:29:16
        return this.p;
    }

    /**
     * @return the field name of join field1. Should be quantified by
     * alias or table name.
     */
    public String getJoinField1Name() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:30:45
        return this.child1.getTupleDesc().getFieldName(this.p.getField1());
    }

    /**
     * @return the field name of join field2. Should be quantified by
     * alias or table name.
     */
    public String getJoinField2Name() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:30:56
        return this.child2.getTupleDesc().getFieldName(this.p.getField2());
    }

    /**
     * @see TupleDesc#merge(TupleDesc, TupleDesc) for possible
     * implementation logic.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:31:39
        return TupleDesc.merge(this.child1.getTupleDesc(), this.child2.getTupleDesc());
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:32:24
        this.child1.open();
        this.child2.open();
        super.open();
    }

    public void close() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:35:38
        super.close();
        this.child1.close();
        this.child2.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 16:18:44
        this.child1.rewind();
        this.child2.rewind();

    }

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, if an equality predicate is used there will be two
     * copies of the join attribute in the results. (Removing such duplicate
     * columns can be done with an additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 16:47:09
        // temp用于保存child1的元组，遍历完child2后置空，准备遍历下一个child1的元组
        TupleDesc td1 = this.child1.getTupleDesc();
        TupleDesc td2 = this.child2.getTupleDesc();
        while (child1.hasNext() || temp != null) {
            if (child1.hasNext() && temp == null) {
                temp = child1.next();
            }
            while (child2.hasNext()) {
                Tuple t2 = child2.next();
                if (this.p.filter(temp, t2)) {
                    Tuple newTuple = new Tuple(newTupleDesc);
                    newTuple.setRecordId(temp.getRecordId());
                    int i = 0;
                    for (; i < td1.numFields(); i++) {
                        newTuple.setField(i, temp.getField(i));
                    }
                    for (int j = 0; j < td2.numFields(); j++) {
                        newTuple.setField(i + j, t2.getField(j));
                    }
                    // 遍历完t2后重置，t置空，准备遍历下一个
                    if (!child2.hasNext()) {
                        child2.rewind();
                        temp = null;
                    }
                    return newTuple;
                }
            }
            // 重置 child2
            child2.rewind();
            temp = null;
        }
        return null;
    }

    @Override
    public OpIterator[] getChildren() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 16:47:32
        return new OpIterator[]{this.child1, this.child2};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 16:48:49
        this.child1 = children[0];
        this.child2 = children[1];
    }

}
