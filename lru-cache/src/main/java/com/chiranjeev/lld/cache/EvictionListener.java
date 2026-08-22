package com.chiranjeev.lld.cache;
@FunctionalInterface public interface EvictionListener<K,V> { void onEviction(K key,V value); static <K,V> EvictionListener<K,V> noop(){return (k,v)->{};} }
