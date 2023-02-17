package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.IntField;
import simpledb.storage.StringField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import  simpledb.storage.TupleIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */
    private int gbField;
    private Type gbFieldType;
    private int aggrField;
    private Op what;
    private CountHandler countHandler;
    private static final String NO_GROUPING_KEY = "NO_GROUPING_KEY";
    public StringAggregator(int gbfield, Type gbfieldtype, int aggrField, Op what) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 15:01:26
        if(what != Op.COUNT){
            throw new IllegalArgumentException("unsupported operator");
        }

        this.gbField = gbfield;
        this.gbFieldType = gbfieldtype;
        this.aggrField = aggrField;
        this.what = what;
        switch (what){
            case COUNT:
                countHandler = new CountHandler();
                break;
            default:
                throw new IllegalArgumentException("unsupported operator");
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 15:28:34
        if(this.gbFieldType!=null&&(!tup.getTupleDesc().getFieldType(aggrField).equals(Type.STRING_TYPE))){
            throw new IllegalArgumentException("unsupported type");
        }
        String key = null;
        if(gbField != NO_GROUPING){
            key = tup.getField(gbField).toString();
        }else{
            key = NO_GROUPING_KEY;
        }
        countHandler.handle(key);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *         aggregateVal) if using group, or a single (aggregateVal) if no
     *         grouping. The aggregateVal is determined by the type of
     *         aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-14 16:24:05
        Map<String, Integer> result = countHandler.getGBResult();
        TupleDesc td = null;
        Type[] types = null;
        String[] names = null;
        List<Tuple> tuples = new ArrayList<>();
        if(gbField != NO_GROUPING){
            td = new TupleDesc(new Type[]{gbFieldType, Type.INT_TYPE}, new String[]{"groupVal", "aggregateVal"});
            types = new Type[]{gbFieldType, Type.INT_TYPE};
            names = new String[]{"groupVal", "aggregateVal"};
            for(Map.Entry<String, Integer> entry : result.entrySet()){
                Tuple tuple = new Tuple(td);
                if(gbFieldType == Type.INT_TYPE) {
                    tuple.setField(this.gbField, new IntField(Integer.parseInt(entry.getKey())));
                }else if(gbFieldType == Type.STRING_TYPE){
                    tuple.setField(this.gbField, new StringField(entry.getKey(), Type.STRING_LEN));
                }
                tuple.setField(this.aggrField, new IntField(entry.getValue()));
                tuples.add(tuple);
            }
        }else {
            td = new TupleDesc(new Type[]{Type.INT_TYPE}, new String[]{"aggregateVal"});
            types = new Type[]{Type.INT_TYPE};
            names = new String[]{"aggregateVal"};
            for(Integer value : result.values()){
                Tuple tuple = new Tuple(td);
                tuple.setField(0,new IntField(value));
                tuples.add(tuple);
            }
        }
        return new TupleIterator(td, tuples);
    }

    private class CountHandler{
        ConcurrentHashMap<String, Integer> gbResult;
        public CountHandler(){
            gbResult = new ConcurrentHashMap<>();
        }
        public void handle(String key){
            if(gbResult.containsKey(key)){
                gbResult.put(key, gbResult.get(key) + 1);
            }else{
                gbResult.put(key, 1);
            }
        }
        public Map<String,Integer> getGBResult(){
            return gbResult;
        }
    }
}
