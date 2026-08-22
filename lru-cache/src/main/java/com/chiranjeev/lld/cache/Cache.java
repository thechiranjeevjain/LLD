package com.chiranjeev.lld.cache;
import java.util.Optional;
public interface Cache<K,V> { Optional<V> get(K key); void put(K key,V value); Optional<V> remove(K key); int size(); CacheStats stats(); }
