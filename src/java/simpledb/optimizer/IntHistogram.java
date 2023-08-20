package simpledb.optimizer;

import simpledb.execution.Predicate;

/**
 * A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

    /**
     * Create a new IntHistogram.
     * <p>
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * <p>
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * <p>
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't
     * simply store every value that you see in a sorted list.
     *
     * @param buckets The number of buckets to split the input value into.
     * @param min     The minimum integer value that will ever be passed to this class for histogramming
     * @param max     The maximum integer value that will ever be passed to this class for histogramming
     */
    private int max;
    private int min;
    private double width;
    private int[] buckets;
    private int tupleNum = 0;
    public IntHistogram(int buckets, int min, int max) {
        // TODO: some code goes here
        this.max=max;
        this.min=min;
        this.width = (1.+max-min)/buckets;
        this.buckets = new int[buckets];
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     *
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
        // TODO: some code goes here
        if(v < min || v > max) throw new IllegalArgumentException("value {" + v + "} is illegal");
        int index = (int) ((v-min)/width);
        buckets[index]++;
        tupleNum++;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * <p>
     * For example, if "op" is "GREATER_THAN" and "v" is 5,
     * return your estimate of the fraction of elements that are greater than 5.
     *
     * @param op Operator
     * @param v  Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

        // TODO: some code goes here
        int index = (int)((v-min)/width);
        switch(op) {
            case GREATER_THAN:
                if(v < min) return 1.0;
                if(v >= max) return 0.0;
                double right_part_count = 1.0*width-(v - index * width - min + 1);
                double sum=0;
                for(int i=index+1;i<buckets.length;i++) {
                    sum+=buckets[i];
                }
                return (right_part_count+sum)/tupleNum;
            case GREATER_THAN_OR_EQ:
                return estimateSelectivity(Predicate.Op.GREATER_THAN, v-1);
            case LESS_THAN:
                return 1-estimateSelectivity(Predicate.Op.GREATER_THAN_OR_EQ, v);
            case LESS_THAN_OR_EQ:
                return 1-estimateSelectivity(Predicate.Op.GREATER_THAN, v);
            case EQUALS:
                if(v < min || v > max) return 0.0;
                return estimateSelectivity(Predicate.Op.GREATER_THAN_OR_EQ, v) - estimateSelectivity(Predicate.Op.GREATER_THAN, v);
            case NOT_EQUALS:
                return 1-estimateSelectivity(Predicate.Op.EQUALS, v);
            default:
                throw new IllegalArgumentException("operator {" + op + "} is illegal");
        }
    }

    /**
     * @return the average selectivity of this histogram.
     *         <p>
     *         This is not an indispensable method to implement the basic
     *         join optimization. It may be needed if you want to
     *         implement a more efficient optimization
     */
    public double avgSelectivity() {
        // TODO: some code goes here
        // some code goes here
        int cnt = 0;
        for (int bucket : buckets) {
            cnt += bucket;
        }
        return 1.0 * cnt / tupleNum;

    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // TODO: some code goes here
        return String.format("IntHistogram{max=%d, min=%d, width=%d, buckets=%s, tupleNum=%d}", max, min, width, buckets, tupleNum);
    }
}
