package com.chiranjeev.lld.cache;
public final class LruCacheDemo {private LruCacheDemo(){}public static void main(String[] args){LruCache<String,Integer> c=new LruCache<>(2,(k,v)->System.out.println("evicted "+k));c.put("A",1);c.put("B",2);c.get("A");c.put("C",3);System.out.println(c.keysMostRecentFirst()+" "+c.stats());}}
