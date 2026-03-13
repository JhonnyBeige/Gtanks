/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class FastHashMap<K, V>
implements Iterable<V> {
    private ConcurrentHashMap<K, V> map = new ConcurrentHashMap();

    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        this.map.put(key, value);
    }

    public void remove(Object key) {
        if (key == null) {
            return;
        }
        this.map.remove(key);
    }

    public void remove(Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        this.map.remove(key, value);
    }

    public int size() {
        return this.map.size();
    }

    public void clear() {
        this.map.clear();
    }

    public V get(Object key) {
        V value = null;
        if (key != null && this.map.containsKey(key)) {
            value = this.map.get(key);
        }
        return value;
    }

    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        return this.map.containsKey(key);
    }

    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Iterator<V> iterator() {
        return this.map.values().iterator();
    }
}

