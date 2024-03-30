package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.DeadlockException;
import simpledb.common.Permissions;
import simpledb.transaction.LockManager;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;
import simpledb.storage.LRUCache;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 *
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /**
     * Bytes per page, including header.
     */
    private static final int DEFAULT_PAGE_SIZE = 4096;

    private static int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * Default number of pages passed to the constructor. This is used by
     * other classes. BufferPool should use the numPages argument to the
     * constructor instead.
     */
    public static final int DEFAULT_PAGES = 50;
    private int numPages;
    private LRUCache<PageId,Page> pages;
    private LockManager lockManager;
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-01-30 16:57:20
        this.pages = new LRUCache<>(numPages);
        this.numPages=numPages;
        this.lockManager= new LockManager();
    }

    public static int getPageSize() {
        return pageSize;
    }

    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
        BufferPool.pageSize = pageSize;
    }

    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void resetPageSize() {
        BufferPool.pageSize = DEFAULT_PAGE_SIZE;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, a page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid  the ID of the transaction requesting the page
     * @param tid  the ID of the transaction requesting the page
     * @param pid  the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public Page getPage(TransactionId tid, PageId pid, Permissions perm)
            throws TransactionAbortedException, DbException {
        // TODO: some code goes here
        // Done by Huangyihang in 2023-02-05 11:28:26

        boolean lockAcquired = false;
        long start = System.currentTimeMillis();
        long timeout = new Random().nextInt(1000) + 1000;
        while (!lockAcquired) {
            long now = System.currentTimeMillis();
            if (now - start > timeout) {
                throw new TransactionAbortedException();
            }
            lockAcquired = lockManager.acquireLock(tid, pid, perm);
        }
        if(this.pages.get(pid)==null){
            DbFile dbFile = Database.getCatalog().getDatabaseFile(pid.getTableId());
            Page page = dbFile.readPage(pid);
            if(this.pages.getSize()>=this.numPages){
                evictPage();
            }
            this.pages.put(pid, page);
            return page;
        }
        return this.pages.get(pid);
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public void unsafeReleasePage(TransactionId tid, PageId pid) {
        // TODO: some code goes here
        // not necessary for lab1|lab2
        lockManager.releaseLock(tid, pid);
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) {
        // TODO: some code goes here
        // not necessary for lab1|lab2
        transactionComplete(tid, true);
    }

    /**
     * Return true if the specified transaction has a lock on the specified page
     */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // TODO: some code goes here
        // not necessary for lab1|lab2
        return lockManager.holdsLock(tid, p);
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid    the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(TransactionId tid, boolean commit) {
        // TODO: some code goes here
        // not necessary for lab1|lab2
        if (commit) {
            try {
                flushPages(tid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            rollback(tid);
        }
        lockManager.releaseAllLock(tid);
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other
     * pages that are updated (Lock acquisition is not needed for lab2).
     * May block if the lock(s) cannot be acquired.
     * <p>
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have
     * been dirtied to the cache (replacing any existing versions of those pages) so
     * that future requests see up-to-date pages.
     *
     * @param tid     the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t       the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        // not necessary for lab1
        DbFile databaseFile = Database.getCatalog().getDatabaseFile(tableId);
        List<Page> afterModified = databaseFile.insertTuple(tid, t);
        for (Page page : afterModified) {    //用脏页替换buffer中现有的页
            page.markDirty(true, tid);
            pages.put(page.getId(), page);
        }
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     * <p>
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have
     * been dirtied to the cache (replacing any existing versions of those pages) so
     * that future requests see up-to-date pages.
     *
     * @param tid the transaction deleting the tuple.
     * @param t   the tuple to delete
     */
    public void deleteTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        // not necessary for lab1
        PageId pageId = t.getRecordId().getPageId();
        int tableId = pageId.getTableId();
        DbFile databaseFile = Database.getCatalog().getDatabaseFile(tableId);
        List<Page> pages = databaseFile.deleteTuple(tid, t);
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).markDirty(true, tid);
        }
    }

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     * break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // TODO: some code goes here
        // not necessary for lab1
        LRUCache<PageId, Page>.DLinkNode head = pages.getHead();
        LRUCache<PageId, Page>.DLinkNode tail = pages.getTail();
        while (head != tail) {
            Page value = head.value;
            if (value != null && value.isDirty() != null) {

                DbFile databaseFile = Database.getCatalog().getDatabaseFile(value.getId().getTableId());
                try {
                    Database.getLogFile().logWrite(value.isDirty(), value.getBeforeImage(), value);
                    Database.getLogFile().force();
                    //这里不能将脏页标记为不脏，如果这样做则当事务提交的时候，flushpage函数找不到脏页，无法将更新写入磁盘
                    //也无法setbeforeimage 详情见LogTest的78行
                    // value.markDirty(false, null);
                    databaseFile.writePage(value);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            head = head.next;
        }
    }

    /**
     * Remove the specific page id from the buffer pool.
     * Needed by the recovery manager to ensure that the
     * buffer pool doesn't keep a rolled back page in its
     * cache.
     * <p>
     * Also used by B+ tree files to ensure that deleted pages
     * are removed from the cache so they can be reused safely
     */
    public synchronized void removePage(PageId pid) {
        // TODO: some code goes here
        // not necessary for lab1

        LRUCache<PageId, Page>.DLinkNode head = pages.getHead();
        LRUCache<PageId, Page>.DLinkNode tail = pages.getTail();
        while (head != tail) {
            PageId key = head.key;
            if (key != null && key.equals(pid)) {
                pages.remove(head);
                return;
            }
            head = head.next;
        }

    }

    /**
     * Flushes a certain page to disk
     *
     * @param pid an ID indicating the page to flush
     */
    private synchronized void flushPage(PageId pid) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1
        Page discard = pages.get(pid);
        DbFile databaseFile = Database.getCatalog().getDatabaseFile(discard.getId().getTableId());
        try {
            TransactionId dirtier = discard.isDirty();
            if (dirtier != null) {
                Database.getLogFile().logWrite(dirtier, discard.getBeforeImage(), discard);
                Database.getLogFile().force();
                discard.markDirty(false, null);
                databaseFile.writePage(discard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write all pages of the specified transaction to disk.
     */
    public synchronized void flushPages(TransactionId tid) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1|lab2
        LRUCache<PageId, Page>.DLinkNode head = pages.getHead();
        LRUCache<PageId, Page>.DLinkNode tail = pages.getTail();
        while (head != tail) {
            Page value = head.value;
            if (value != null && value.isDirty() != null && value.isDirty().equals(tid)) {
                DbFile databaseFile = Database.getCatalog().getDatabaseFile(value.getId().getTableId());
                try {
                    Database.getLogFile().logWrite(value.isDirty(), value.getBeforeImage(), value);
                    Database.getLogFile().force();
                    value.markDirty(false, null);
                    databaseFile.writePage(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            head = head.next;
        }
    }

    private synchronized void rollback(TransactionId transactionId) {
        LRUCache<PageId, Page>.DLinkNode head = pages.getHead();
        LRUCache<PageId, Page>.DLinkNode tail = pages.getTail();
        while (head != tail) {
            Page value = head.value;
            LRUCache<PageId, Page>.DLinkNode tmp = head.next;
            if (value != null && value.isDirty() != null && value.isDirty().equals(transactionId)) {
                //删掉脏页
                pages.remove(head);
                try {
                    //重新读原来的页
                    Page page = Database.getBufferPool().getPage(transactionId, value.getId(), Permissions.READ_ONLY);
                    page.markDirty(false,null);
                } catch (TransactionAbortedException e) {
                    e.printStackTrace();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            head = tmp;
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized void evictPage() throws DbException {
        // TODO: some code goes here
        // not necessary for lab1
        //如果为脏页则不能替换
        Page value = pages.getTail().prev.value;
        //  如果是脏页
        if (value != null && value.isDirty() != null) {
            LRUCache<PageId, Page>.DLinkNode head = pages.getHead();
            LRUCache<PageId, Page>.DLinkNode tail = pages.getTail();
            tail = tail.prev;
            while (head != tail) {
                Page page = tail.value;
                if (page != null && page.isDirty() == null) {
                    pages.remove(tail);
                    return;
                }
                tail = tail.prev;
            }
        } else {
            //不是脏页没改过，不需要写磁盘
            pages.discard();
        }
    }

}
