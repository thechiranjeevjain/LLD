package com.chiranjeev.lld.cache;
import org.junit.jupiter.api.Test;
import java.util.*;import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {
    @Test void evictsLeastRecentlyUsed(){LruCache<String,Integer> c=new LruCache<>(2);c.put("A",1);c.put("B",2);c.get("A");c.put("C",3);assertEquals(List.of("C","A"),c.keysMostRecentFirst());assertTrue(c.get("B").isEmpty());assertEquals(1,c.stats().evictions());}
    @Test void updateDoesNotGrowAndBecomesMostRecent(){LruCache<String,Integer> c=new LruCache<>(2);c.put("A",1);c.put("B",2);c.put("A",9);assertEquals(2,c.size());assertEquals(9,c.get("A").orElseThrow());assertEquals(List.of("A","B"),c.keysMostRecentFirst());}
    @Test void listenerRunsForCapacityEviction(){List<String> evicted=new ArrayList<>();LruCache<String,Integer> c=new LruCache<>(1,(k,v)->evicted.add(k+"="+v));c.put("A",1);c.put("B",2);assertEquals(List.of("A=1"),evicted);}
    @Test void concurrentAccessPreservesCapacityAndLinks() throws Exception {LruCache<Integer,Integer> c=new LruCache<>(50);ExecutorService pool=Executors.newFixedThreadPool(8);try{List<Callable<Void>> work=new ArrayList<>();for(int i=0;i<1_000;i++){int n=i;work.add(()->{c.put(n%100,n);c.get((n+7)%100);return null;});}for(Future<Void> f:pool.invokeAll(work))f.get();}finally{pool.shutdown();}assertTrue(c.size()<=50);assertEquals(c.size(),new HashSet<>(c.keysMostRecentFirst()).size());}
    @Test void rejectsNullsAndBadCapacity(){assertThrows(IllegalArgumentException.class,()->new LruCache<>(0));LruCache<String,String> c=new LruCache<>(1);assertThrows(NullPointerException.class,()->c.put("A",null));}
}
