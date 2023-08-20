package simpledb.optimizer;

import simpledb.common.Database;
import simpledb.common.Type;
import simpledb.execution.Predicate;
import simpledb.storage.*;
import simpledb.transaction.TransactionId;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query.
 * <p>
 * This class is not needed in implementing lab1 and lab2.
 */
public class TableStats {

    private static final ConcurrentMap<String, TableStats> statsMap = new ConcurrentHashMap<>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }

    private final int tableid;

    private final int ioCostPerPage;

    private final TupleDesc tupleDesc;

    private Object[] histogram;

    private int tupleNum;

    private int pageNum;



    public static void setStatsMap(Map<String, TableStats> s) {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics(){
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    /**
     * Number of bins for the histogram. Feel free to increase this value over
     * 100, though our tests assume that you have at least 100 bins in your
     * histograms.
     */
    static final int NUM_HIST_BINS = 100;

    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     *
     * @param tableid       The table over which to compute statistics
     * @param ioCostPerPage The cost per page of IO. This doesn't differentiate between
     *                      sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage)  {
        // For this function, you'll have to get the
        // DbFile for the table in question,
        // then scan through its tuples and calculate
        // the values that you need.
        // You should try to do this reasonably efficiently, but you don't
        // necessarily have to (for example) do everything
        // in a single scan of the table.
        // TODO: some code goes here
        this.tableid = tableid;
        this.ioCostPerPage = ioCostPerPage;
        this.tupleDesc = Database.getCatalog().getTupleDesc(tableid);
        HeapFile dbFile = (HeapFile) Database.getCatalog().getDatabaseFile(tableid);
        this.pageNum = ((HeapFile) dbFile).numPages();
        int [] min = new int[this.tupleDesc.numFields()];
        int [] max = new int[this.tupleDesc.numFields()];
        DbFileIterator it = dbFile.iterator(new TransactionId());
        this.histogram = new Object[this.tupleDesc.numFields()];
        try{
            it.open();
            while(it.hasNext()) {
                Tuple tuple = it.next();
                this.tupleNum++;
                for(int i = 0; i < this.tupleDesc.numFields(); i++) {
                    if(this.tupleDesc.getFieldType(i) == Type.INT_TYPE) {
                        IntField field = (IntField) tuple.getField(i);
                        int value = field.getValue();
                        max[i] = Math.max(max[i], value);
                        min[i] = Math.min(min[i], value);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < this.tupleDesc.numFields(); i++) {
            if (this.tupleDesc.getFieldType(i).equals(Type.INT_TYPE)) {
                this.histogram[i] = new IntHistogram(NUM_HIST_BINS, min[i], max[i]);
            } else {
                this.histogram[i] = new StringHistogram(NUM_HIST_BINS);
            }
        }

        try{
            it.rewind();
            while (it.hasNext()){
                Tuple tuple = it.next();
                for (int i = 0; i < this.tupleDesc.numFields(); i++) {
                    if (this.tupleDesc.getFieldType(i).equals(Type.INT_TYPE)) {
                        IntField field = (IntField) tuple.getField(i);
                        int value = field.getValue();
                        ((IntHistogram) this.histogram[i]).addValue(value);
                    } else {
                        StringField field = (StringField) tuple.getField(i);
                        String value = field.getValue();
                        ((StringHistogram) this.histogram[i]).addValue(value);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        it.close();
    }

    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is costPerPageIO. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * <p>
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     *
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
        // TODO: some code goes here
        return this.pageNum * this.ioCostPerPage;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     *
     * @param selectivityFactor The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // TODO: some code goes here
        return (int)(this.tupleNum*selectivityFactor);
    }

    /**
     * The average selectivity of the field under op.
     *
     * @param field the index of the field
     * @param op    the operator in the predicate
     *              The semantic of the method is that, given the table, and then given a
     *              tuple, of which we do not know the value of the field, return the
     *              expected selectivity. You may estimate this value from the histograms.
     */
    public double avgSelectivity(int field, Predicate.Op op) {
        // TODO: some code goes here
        if(this.tupleDesc.getFieldType(field) == Type.INT_TYPE) {
            return ((IntHistogram)this.histogram[field]).avgSelectivity();
        }else {
            return ((StringHistogram)this.histogram[field]).avgSelectivity();
        }
    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     *
     * @param field    The field over which the predicate ranges
     * @param op       The logical operation in the predicate
     * @param constant The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // TODO: some code goes here
        if (this.tupleDesc.getFieldType(field) == Type.INT_TYPE) {
            return ((IntHistogram) histogram[field]).estimateSelectivity(op, ((IntField) constant).getValue());
        } else {
            return ((StringHistogram) histogram[field]).estimateSelectivity(op, ((StringField) constant).getValue());
        }
    }

    /**
     * return the total number of tuples in this table
     */
    public int totalTuples() {
        // TODO: some code goes here
        return tupleNum;
    }

}
