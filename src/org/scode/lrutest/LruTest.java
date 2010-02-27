package org.scode.lrutest;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class LruTest {
    public static int CACHE_SIZE = 50000000;
    public static long TARGET_SPEED = 100000;

    public static class SomeObject implements Serializable {
        public int a;
        public int b;
        public int c;
        public String s;
    }

    private LinkedHashMap<Integer,SomeObject> hmap = new LinkedHashMap<Integer,SomeObject>(CACHE_SIZE, (float)0.75, true) {
        @Override protected boolean removeEldestEntry (Map.Entry<Integer,SomeObject> eldest) {
            return (this.size() >= CACHE_SIZE);
        }
    };

    public static void main(String[] args) throws InterruptedException {
        new LruTest().bench();
    }

    public void bench() throws InterruptedException {
        long start = System.currentTimeMillis();
        long lastUpdate = start;

        long count = 0;
        while (true) {
            count++;

            if (count % 1000 == 0) {
                long now = System.currentTimeMillis();
                long elapsed = now - start;
                long target = elapsed * (TARGET_SPEED / 1000);

                if (now - lastUpdate > 1000) {
                    System.out.println("Size: " + hmap.size() / 1000000.0 + "m after " + (count / 1000000.0) + "m iterations");
                    lastUpdate = now;

                    if (count + TARGET_SPEED < target) {
                        System.out.println("target speed not reached: " + count + "/" + target);
                    }
                }

                if (count > target) {
                    Thread.sleep((count - target) / (TARGET_SPEED / 1000));
                }
            }

            int i = (int)(Math.random() * (CACHE_SIZE * 2.0));

            if (!hmap.containsKey(i)) {
                SomeObject o = new SomeObject();

                o.a = 1;
                o.b = 3;
                o.c = 23498234 * i;
                o.s = "reasonably sized non-interned test string, tra la la";

                hmap.put(i, o);
            } else {
                hmap.get(i);
            }
        }
    }
}
