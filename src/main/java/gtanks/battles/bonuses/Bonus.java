/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.bonuses;

import gtanks.battles.bonuses.BonusType;
import gtanks.battles.tanks.math.Vector3;

public class Bonus {
    public Vector3 position;
    public BonusType type;
    public long spawnTime;

    public Bonus(Vector3 position, BonusType type) {
        this.position = position;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }
}

