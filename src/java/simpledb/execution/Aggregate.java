package simpledb.execution;

import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;

import java.util.NoSuchElementException;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    private OpIterator tupIterator;
    private int aggrField;
    private int groupField;
    private Aggregator.Op aop;
    private OpIterator resIterator;
    private Aggregator aggregator;

    /**
     * Constructor.
     * <p>
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     *
     * @param child  The OpIterator that is feeding us tuples.
     * @param afield The column over which we are computing an aggregate.
     * @param groupField The column over which we are grouping the result, or -1 if
     *               there is no grouping
     * @param aop    The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int groupField, Aggregator.Op aop) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:00:00
        this.tupIterator = child;
        this.aggrField = afield;
        this.groupField = groupField;
        this.aop = aop;
        Type gbFieldType = null;
        if(groupField != Aggregator.NO_GROUPING){
            gbFieldType = child.getTupleDesc().getFieldType(groupField);
        }
        switch (child.getTupleDesc().getFieldType(afield)){
            case INT_TYPE:
                aggregator = new IntegerAggregator(groupField, gbFieldType, afield, aop);
                break;
            case STRING_TYPE:
                aggregator = new StringAggregator(groupField, gbFieldType, afield, aop);
                break;
            default:
                throw new IllegalArgumentException("unsupported type");
        }
        try{
            child.open();
            while (child.hasNext()){
                aggregator.mergeTupleIntoGroup(child.next());
            }
            child.close();
        }catch (DbException | TransactionAbortedException e){
            e.printStackTrace();
        }

    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link Aggregator#NO_GROUPING}
     */
    public int groupField() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:00:13
        return this.groupField;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     */
    public String groupFieldName() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:01:48
        if(this.groupField == Aggregator.NO_GROUPING)
            return null;
        else
            return this.tupIterator.getTupleDesc().getFieldName(groupField);
    }

    /**
     * @return the aggregate field
     */
    public int aggregateField() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:02:54
        return this.aggrField;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     */
    public String aggregateFieldName() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:03:51
        return this.tupIterator.getTupleDesc().getFieldName(aggrField);
    }

    /**
     * @return return the aggregate operator
     */
    public Aggregator.Op aggregateOp() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:04:18
        return this.aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
        return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:05:15
        this.resIterator = aggregator.iterator();
        this.resIterator.open();
        super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:06:48
        if(this.resIterator!=null && resIterator.hasNext())
            return this.resIterator.next();
        else
            return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:07:35
        if(this.resIterator != null)
            this.resIterator.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * <p>
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:08:32
        if (this.groupField == Aggregator.NO_GROUPING)
            return new TupleDesc(new Type[]{Type.INT_TYPE},
                    new String[]{this.aop.toString() + "(" + this.tupIterator.getTupleDesc().getFieldName(aggrField) + ")"});
        else
            return new TupleDesc(new Type[]{this.tupIterator.getTupleDesc().getFieldType(groupField), Type.INT_TYPE},
                    new String[]{this.tupIterator.getTupleDesc().getFieldName(groupField), this.aop.toString() + "(" + this.tupIterator.getTupleDesc().getFieldName(aggrField) + ")"});

    }

    public void close() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:10:08
        super.close();
        resIterator.close();
    }

    @Override
    public OpIterator[] getChildren() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:10:44
        return new OpIterator[]{this.tupIterator};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 20:18:04
        try{
            this.tupIterator = children[0];
            this.tupIterator.open();
            while (tupIterator.hasNext()){
                aggregator.mergeTupleIntoGroup(tupIterator.next());
            }
            this.tupIterator.close();
        } catch (TransactionAbortedException |DbException e) {
            throw new RuntimeException(e);
        }
    }

}
