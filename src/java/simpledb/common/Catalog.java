package simpledb.common;

import simpledb.storage.DbFile;
import simpledb.storage.HeapFile;
import simpledb.storage.TupleDesc;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 *
 * @Threadsafe
 */
public class Catalog {

    private final Map<Integer, Table> mapIDTable ;
    private final Map<String, Table> mapNameTable ;


    // a help class
    public class Table implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public String pkeyField;
        public DbFile dbFile;
        public Table(String name, String pkeyField, DbFile dbFile){
            this.name = name;
            this.pkeyField = pkeyField;
            this.dbFile = dbFile;
        }
    }
    /**
     * Constructor.
     * Creates a new, empty catalog.
     */
    public Catalog() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 12:33:21
        this.mapIDTable=new ConcurrentHashMap<>();
        this.mapNameTable=new ConcurrentHashMap<>();
    }

    /**
     * Add a new table to the catalog.
     * This table's contents are stored in the specified DbFile.
     *
     * @param file      the contents of the table to add;  file.getId() is the identfier of
     *                  this file/tupledesc param for the calls getTupleDesc and getFile
     * @param name      the name of the table -- may be an empty string.  May not be null.  If a name
     *                  conflict exists, use the last table to be added as the table for a given name.
     * @param pkeyField the name of the primary key field
     */
    public void addTable(DbFile file, String name, String pkeyField) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 12:36:04
        // Modified by Huangyihang in 2023-01-30 14:41:48
        Table table = new Table(name, pkeyField, file);
        mapIDTable.put(file.getId(), table);
        mapNameTable.put(name, table);

    }

    public void addTable(DbFile file, String name) {
        addTable(file, name, "");
    }

    /**
     * Add a new table to the catalog.
     * This table has tuples formatted using the specified TupleDesc and its
     * contents are stored in the specified DbFile.
     *
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *             this file/tupledesc param for the calls getTupleDesc and getFile
     */
    public void addTable(DbFile file) {
        addTable(file, (UUID.randomUUID()).toString());
    }

    /**
     * Return the id of the table with a specified name,
     *
     * @throws NoSuchElementException if the table doesn't exist
     */
    public int getTableId(String name) throws NoSuchElementException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 14:57:40
        if (!(name!=null && this.mapNameTable.containsKey(name)) ) {
            throw new NoSuchElementException("Table " + name + " does not exist.");
        }
        return this.mapNameTable.get(name).dbFile.getId();
    }

    /**
     * Returns the tuple descriptor (schema) of the specified table
     *
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *                function passed to addTable
     * @throws NoSuchElementException if the table doesn't exist
     */
    public TupleDesc getTupleDesc(int tableid) throws NoSuchElementException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 15:00:58
        if (!this.mapIDTable.containsKey(tableid)) {
            throw new NoSuchElementException("Table " + tableid + " does not exist.");
        }
        return this.mapIDTable.get(tableid).dbFile.getTupleDesc();
    }

    /**
     * Returns the DbFile that can be used to read the contents of the
     * specified table.
     *
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *                function passed to addTable
     */
    public DbFile getDatabaseFile(int tableid) throws NoSuchElementException {
        // TODO: some code goes here
        if(!this.mapIDTable.containsKey(tableid)){
            throw new NoSuchElementException("Table " + tableid + " does not exist.");
        }
        return this.mapIDTable.get(tableid).dbFile;
    }

    public String getPrimaryKey(int tableid) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 15:01:32
        if(!this.mapIDTable.containsKey(tableid)){
            throw new NoSuchElementException("Table " + tableid + " does not exist.");
        }
        return this.mapIDTable.get(tableid).pkeyField;
    }

    public Iterator<Integer> tableIdIterator() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 15:04:22
        return this.mapIDTable.keySet().iterator();
    }

    public String getTableName(int id) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 15:04:44
        return this.mapIDTable.get(id).name;
    }

    /**
     * Delete all tables from the catalog
     */
    public void clear() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 15:05:19
        this.mapIDTable.clear();
    }

    /**
     * Reads the schema from a file and creates the appropriate tables in the database.
     *
     * @param catalogFile
     */
    public void loadSchema(String catalogFile) {
        String line = "";
        String baseFolder = new File(new File(catalogFile).getAbsolutePath()).getParent();
        try {
            BufferedReader br = new BufferedReader(new FileReader(catalogFile));

            while ((line = br.readLine()) != null) {
                //assume line is of the format name (field type, field type, ...)
                String name = line.substring(0, line.indexOf("(")).trim();
                //System.out.println("TABLE NAME: " + name);
                String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String[] els = fields.split(",");
                List<String> names = new ArrayList<>();
                List<Type> types = new ArrayList<>();
                String primaryKey = "";
                for (String e : els) {
                    String[] els2 = e.trim().split(" ");
                    names.add(els2[0].trim());
                    if (els2[1].trim().equalsIgnoreCase("int"))
                        types.add(Type.INT_TYPE);
                    else if (els2[1].trim().equalsIgnoreCase("string"))
                        types.add(Type.STRING_TYPE);
                    else {
                        System.out.println("Unknown type " + els2[1]);
                        System.exit(0);
                    }
                    if (els2.length == 3) {
                        if (els2[2].trim().equals("pk"))
                            primaryKey = els2[0].trim();
                        else {
                            System.out.println("Unknown annotation " + els2[2]);
                            System.exit(0);
                        }
                    }
                }
                Type[] typeAr = types.toArray(new Type[0]);
                String[] namesAr = names.toArray(new String[0]);
                TupleDesc t = new TupleDesc(typeAr, namesAr);
                HeapFile tabHf = new HeapFile(new File(baseFolder + "/" + name + ".dat"), t);
                addTable(tabHf, name, primaryKey);
                System.out.println("Added table : " + name + " with schema " + t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Invalid catalog entry : " + line);
            System.exit(0);
        }
    }
}

