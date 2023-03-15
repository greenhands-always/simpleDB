package simpledb.storage;

import simpledb.common.Database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<Key,Value> {
    class DLinkNode{
        Key key;
        Value value;
        DLinkNode prev;
        DLinkNode next;
        public DLinkNode() {}
        public DLinkNode(Key _key, Value _value) {key = _key; value = _value;}
    }
    private Map<Key,DLinkNode> cache = new ConcurrentHashMap<>();
    private int size;
    private int capacity;
    private DLinkNode head,tail;
    public LRUCache(int capacity){
        this.size = 0;
        this.capacity = capacity;
        head = new DLinkNode();
        tail = new DLinkNode();
        head.next = tail;
        head.prev = tail;
        tail.prev = head;
        tail.next = head;
    }
    public int getSize(){
        return size;
    }
    public  DLinkNode getHead(){
        return head;
    }

    public DLinkNode getTail() {
        return tail;
    }
    public Map<Key,DLinkNode> getCache(){
        return cache;
    }

    public Value get(Key key){
        DLinkNode node = cache.get(key);
        if(node == null){
            return null;
        }
        moveToHead(node);
        return node.value;
    }
    public synchronized void remove(DLinkNode node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
        cache.remove(node.key);
        size--;
    }

    public synchronized void discard(){
        // 如果超出容量，删除双向链表的尾部节点
        DLinkNode tail = removeTail();
        // 删除哈希表中对应的项
        cache.remove(tail.key);
        size--;

    }
    public synchronized void put(Key key, Value value) {
        DLinkNode node = cache.get(key);
        if (node == null) {
            // 如果 key 不存在，创建一个新的节点
            DLinkNode newNode = new DLinkNode(key, value);
            // 添加进哈希表
            cache.put(key, newNode);
            // 添加至双向链表的头部
            addToHead(newNode);
            ++size;
        }
        else {
            // 如果 key 存在，先通过哈希表定位，再修改 value，并移到头部
            node.value = value;
            moveToHead(node);
        }
    }

    private void addToHead(DLinkNode node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(DLinkNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }


    private void moveToHead(DLinkNode node) {
        removeNode(node);
        addToHead(node);
    }

    private DLinkNode removeTail() {
        DLinkNode res = tail.prev;
        removeNode(res);
        return res;
    }
}
