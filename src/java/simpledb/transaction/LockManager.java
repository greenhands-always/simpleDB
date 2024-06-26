package simpledb.transaction;

import simpledb.common.Permissions;
import simpledb.storage.PageId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LockManager {
    ConcurrentHashMap<PageId, ArrayList<Lock>> mapPageLocks;

    public LockManager() {
        mapPageLocks = new ConcurrentHashMap<>();
    }

    /**
     * 获取锁
     * @param tid
     * @param pageId
     * @param permissions
     * @return
     */
    public synchronized  Boolean acquireLock(TransactionId tid, PageId pageId, Permissions permissions){
        Lock lock = new Lock(tid, permissions);
        ArrayList<Lock> locks = mapPageLocks.get(pageId);
        // 页面没有锁则创建锁
        if(locks==null || locks.size()==0){
            locks = new ArrayList<>();
            locks.add(lock);
            mapPageLocks.put(pageId,locks);
            return true;
        }


        if(locks.size()==1){
            //当只有一个事务抢占锁
            Lock curLock = locks.get(0);
            if(curLock.getTransactionId().equals(tid)){
                //判断是否进行锁升级
                if(curLock.getPermissions().equals(Permissions.READ_ONLY) && lock.getPermissions().equals(Permissions.READ_WRITE)){
                    curLock.setPermissions(Permissions.READ_WRITE);
                }
                return true;
            }else{
                if(curLock.getPermissions().equals(Permissions.READ_ONLY) && lock.getPermissions().equals(Permissions.READ_ONLY)){
                    locks.add(lock);
                    return true;
                }
                return false;
            }
        }

        //当有多个事务抢占锁，说明必然是多个读事务
        if(lock.getPermissions().equals(Permissions.READ_WRITE)){
            return false;
        }

        //该事务如果
        for(Lock l: locks){
            if(l.getTransactionId().equals(lock.getTransactionId())){
                return true;
            }
        }
        locks.add(lock);
        return true;
    }
    /**
     * 释放锁
     * @param tid
     * @param pageId
     */
    public synchronized  void releaseLock(TransactionId tid,PageId pageId){
        ArrayList<Lock> locks = mapPageLocks.get(pageId);
        for(Lock l:locks){
            if(l.getTransactionId().equals(tid)){
                locks.remove(l);
                if(locks.size()==0){
                    mapPageLocks.remove(pageId);
                }
                return;
            }
        }
    }

    /**
     * 释放当前事务的所有锁
     * @param tid
     */
    public synchronized  void releaseAllLock(TransactionId tid){
        for(PageId pid: mapPageLocks.keySet()){
            ArrayList<Lock> locks = mapPageLocks.get(pid);
            for(Lock lock:locks){
                if(lock.getTransactionId().equals(tid)){
                    locks.remove(lock);
                    if(locks.size()==0){
                        mapPageLocks.remove(pid);
                    }
                    break;
                }
            }
        }
    }

    public synchronized Boolean holdsLock(TransactionId tid, PageId p) {
        List<Lock> locks = mapPageLocks.get(p.getPageNumber());
        for (int i = 0; i < locks.size(); i++) {
            Lock lock = locks.get(i);
            if (lock.getTransactionId().equals(tid)) {
                return true;
            }
        }
        return false;
    }



}
