/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.params;

import java.util.ArrayList;
import java.util.Collections;

public class OnlineStats {
    private static int online;
    private static int battlesOnline;
    private static final ArrayList<Integer> stat;

    public static int getOnline() {
        return online;
    }

    public static int getInBattlesOnline() {
        return battlesOnline;
    }

    public static void addOnline() {
        stat.add(++online);
    }

    public static void removeInBattleOnline() {
        --battlesOnline;
    }

    public static void addInBattleOnline() {
        ++battlesOnline;
    }

    public static void removeOnline() {
        --online;
    }

    public static int getMaxOnline() {
        return stat.size() == 0 ? 0 : Collections.max(stat);
    }

    static {
        stat = new ArrayList();
    }
}

