/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.utils;

public class StringUtils {
    public static String concatStrings(String ... str) {
        StringBuffer sbf = new StringBuffer();
        String[] var5 = str;
        int var4 = str.length;
        for (int var3 = 0; var3 < var4; ++var3) {
            String adder = var5[var3];
            sbf.append(adder);
        }
        return sbf.toString();
    }

    public static String concatMassive(String[] src, int start) {
        StringBuffer sbf = new StringBuffer();
        for (int i = start; i < src.length; ++i) {
            sbf.append(src[i]);
            if (i == src.length - 1) continue;
            sbf.append(' ');
        }
        return sbf.toString();
    }
}

