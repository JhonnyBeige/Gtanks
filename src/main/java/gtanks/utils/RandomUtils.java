/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.utils;

public class RandomUtils {
    public static float getRandom(float min, float max) {
        return min == max ? min : (float)((double)min + Math.random() * (double)(max - min + 1.0f));
    }
}

