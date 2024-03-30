package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

    private File f;
    private TupleDesc td;
    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 20:30:17
        this.f=f;
        this.td=td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 20:31:56
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 20:33:12
        return this.f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 20:35:20
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 21:14:29
        int pgNo = pid.getPageNumber();
        int tableId = pid.getTableId();
        int pageSize = BufferPool.getPageSize();
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.f,"r");
            if((pgNo+1)*pageSize>f.length()){
                throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
            }
            byte[] data = new byte[pageSize];
            randomAccessFile.seek((long)pgNo*pageSize);
            int read = randomAccessFile.read(data,0,pageSize);
            if(read==-1)
                throw new NoSuchElementException("no such page");
            if(read!=pageSize){
                throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
            }
            randomAccessFile.close();
            return new HeapPage((HeapPageId)pid,data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1

        HeapPageId heapPageId = (HeapPageId) page.getId();
        int size = BufferPool.getPageSize();
        int pageNumber = heapPageId.getPageNumber();
        byte[] pageData = page.getPageData();
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw")) {
            randomAccessFile.seek(pageNumber * size);
            randomAccessFile.write(pageData);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-07 21:15:58
        long length = this.f.length();
        return ((int) Math.ceil(length * 1.0 / BufferPool.getPageSize()));
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        // not necessary for lab1
        if (!getFile().canRead() || !getFile().canWrite()) {
            throw new IOException();
        }
        List<Page> res = new ArrayList<>();
        HeapPage page;
        HeapPageId heapPageId;
        int i;
        for (i = 0; i < numPages(); i++) {
            heapPageId = new HeapPageId(getId(), i);
            page = (HeapPage) Database.getBufferPool().getPage(tid, heapPageId, Permissions.READ_ONLY);

            if(page==null){
                Database.getBufferPool().unsafeReleasePage(tid, heapPageId);
                continue;
            }
            if (page.getNumUnusedSlots() == 0) {
                Database.getBufferPool().unsafeReleasePage(tid, heapPageId);
                continue;
            }
            page.insertTuple(t);
            page.markDirty(true, tid);
            res.add(page);
            return res;
        }
        heapPageId = new HeapPageId(getId(), i);
        page = new HeapPage(heapPageId, HeapPage.createEmptyPageData());
        //-------注：这两句话不能颠倒位置，现在操作系统内存中插入tuple然后写入磁盘，
        // 如果先写入磁盘在插入tuple则更新没写进去磁盘
        page.insertTuple(t);
        writePage(page);
        //-------
        res.add(page);
        return res;

    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        // not necessary for lab1
        ArrayList<Page> res = new ArrayList<>();
        HeapPageId heapPageId  = (HeapPageId) t.getRecordId().getPageId();
        HeapPage heapPage = (HeapPage) Database.getBufferPool().getPage(tid,heapPageId,Permissions.READ_WRITE);
        if(heapPage==null){
            throw  new DbException("null");
        }
        heapPage.deleteTuple(t);
        res.add(heapPage);
        return res;
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-08 00:00:11
        return new HeapFileIterator(tid,Permissions.READ_ONLY);
    }
    /**
     * 辅助类
     * 一页一页的读和遍历文件
     */
    public class HeapFileIterator implements DbFileIterator{
        TransactionId tid;
        Permissions permissions;
        BufferPool bufferPool =Database.getBufferPool();
        Iterator<Tuple> iterator;  //页内迭代器
        int num = 0;

        public HeapFileIterator(TransactionId tid,Permissions permissions){
            this.tid = tid;
            this.permissions = permissions;
        }

        /**
         * 从第一页开始打开文件进行遍历
         * @throws DbException
         * @throws TransactionAbortedException
         */
        @Override
        public void open() throws DbException, TransactionAbortedException {
            num = 0;
            HeapPageId heapPageId = new HeapPageId(getId(), num);
            HeapPage page = (HeapPage)this.bufferPool.getPage(tid, heapPageId, permissions);
            if(page==null){
                throw  new DbException("page null");
            }else{
                iterator = page.iterator();
            }
        }

        /**
         * 获取下一有数据的页,成功返回true,失败返回false
         * @return
         * @throws DbException
         * @throws TransactionAbortedException
         */
        public boolean nextPage() throws DbException, TransactionAbortedException {
            while(true){
                num++;
                if(num>=numPages()){
                    return false;
                }
                HeapPageId heapPageId = new HeapPageId(getId(), num);
                HeapPage page = (HeapPage)bufferPool.getPage(tid,heapPageId,permissions);
                if(page==null){
                    continue;
                }
                iterator = page.iterator();
                if(iterator.hasNext()){
                    return true;
                }
            }
        }



        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if(iterator==null){
                return false;
            }
            if(iterator.hasNext()){
                return true;
            }else{
                return nextPage();
            }
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if(iterator==null){
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            open();
        }

        @Override
        public void close() {
            iterator = null;
        }
    }

}


