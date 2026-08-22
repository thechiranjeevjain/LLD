package com.chiranjeev.lld.cache;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/** Hash index + intrusive doubly-linked recency list; all mutations share one lock. */
public final class LruCache<K,V> implements Cache<K,V> {
    private final int capacity; private final Map<K,Node<K,V>> index=new HashMap<>(); private final ReentrantLock lock=new ReentrantLock();
    private final Node<K,V> head=new Node<>(null,null),tail=new Node<>(null,null); private final EvictionListener<K,V> listener;
    private long hits,misses,evictions;
    public LruCache(int capacity){this(capacity,EvictionListener.noop());}
    public LruCache(int capacity,EvictionListener<K,V> listener){if(capacity<=0)throw new IllegalArgumentException("capacity must be positive");this.capacity=capacity;this.listener=Objects.requireNonNull(listener);head.next=tail;tail.previous=head;}
    public Optional<V> get(K key){Objects.requireNonNull(key);lock.lock();try{Node<K,V> n=index.get(key);if(n==null){misses++;return Optional.empty();}hits++;moveToFront(n);return Optional.of(n.value);}finally{lock.unlock();}}
    public void put(K key,V value){Objects.requireNonNull(key);Objects.requireNonNull(value);K evictedKey=null;V evictedValue=null;lock.lock();try{Node<K,V> existing=index.get(key);if(existing!=null){existing.value=value;moveToFront(existing);return;}Node<K,V> added=new Node<>(key,value);index.put(key,added);addFirst(added);if(index.size()>capacity){Node<K,V> victim=tail.previous;unlink(victim);index.remove(victim.key);evictions++;evictedKey=victim.key;evictedValue=victim.value;}}finally{lock.unlock();}if(evictedKey!=null)listener.onEviction(evictedKey,evictedValue);}
    public Optional<V> remove(K key){Objects.requireNonNull(key);lock.lock();try{Node<K,V> n=index.remove(key);if(n==null)return Optional.empty();unlink(n);return Optional.of(n.value);}finally{lock.unlock();}}
    public int size(){lock.lock();try{return index.size();}finally{lock.unlock();}}
    public CacheStats stats(){lock.lock();try{return new CacheStats(hits,misses,evictions);}finally{lock.unlock();}}
    public List<K> keysMostRecentFirst(){lock.lock();try{List<K> keys=new ArrayList<>();for(Node<K,V> n=head.next;n!=tail;n=n.next)keys.add(n.key);return List.copyOf(keys);}finally{lock.unlock();}}
    private void moveToFront(Node<K,V> n){unlink(n);addFirst(n);} private void addFirst(Node<K,V> n){n.next=head.next;n.previous=head;head.next.previous=n;head.next=n;} private void unlink(Node<K,V> n){n.previous.next=n.next;n.next.previous=n.previous;}
    private static final class Node<K,V>{private final K key;private V value;private Node<K,V> previous,next;private Node(K key,V value){this.key=key;this.value=value;}}
}
