/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.quartz;

public enum TimeType {
    MS(1),
    SEC(1000),
    MIN(60000),
    HOUR(3600000);

    private final int mn;

    private TimeType(int mn) {
        this.mn = mn;
    }

    public long time(long time) {
        return (long)this.mn * time;
    }
}

