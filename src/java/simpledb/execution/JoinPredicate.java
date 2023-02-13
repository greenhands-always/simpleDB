package simpledb.execution;

import simpledb.storage.Field;
import simpledb.storage.Tuple;

import java.io.Serializable;

/**
 * JoinPredicate compares fields of two tuples using a predicate. JoinPredicate
 * is most likely used by the Join operator.
 */
public class JoinPredicate implements Serializable {

    private static final long serialVersionUID = 1L;
    private  int field1;
    private  int field2;
    private  Predicate.Op op;
    /**
     * Constructor -- create a new predicate over two fields of two tuples.
     *
     * @param field1 The field index into the first tuple in the predicate
     * @param field2 The field index into the second tuple in the predicate
     * @param op     The operation to apply (as defined in Predicate.Op); either
     *               Predicate.Op.GREATER_THAN, Predicate.Op.LESS_THAN,
     *               Predicate.Op.EQUAL, Predicate.Op.GREATER_THAN_OR_EQ, or
     *               Predicate.Op.LESS_THAN_OR_EQ
     * @see Predicate
     */
    public JoinPredicate(int field1, Predicate.Op op, int field2) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 10:53:27
        this.field1 = field1;
        this.field2 = field2;
        this.op = op;
    }

    /**
     * Apply the predicate to the two specified tuples. The comparison can be
     * made through Field's compare method.
     *
     * @return true if the tuples satisfy the predicate.
     */
    public boolean filter(Tuple t1, Tuple t2) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:05:01
        return t1.getField(field1).compare(op, t2.getField(field2));
    }

    public int getField1() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:05:12
        return this.field1;
    }

    public int getField2() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:05:18
        return this.field2;
    }

    public Predicate.Op getOperator() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-13 11:05:28
        return this.op;
    }
}
