/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons;

import gtanks.battles.tanks.weapons.EntityType;
import gtanks.battles.tanks.weapons.ShotData;

public interface IEntity {
    public static final int chargingTime = 0;
    public static final int weakeningCoeff = 0;
    public static final float damage_min = 0.0f;
    public static final float damage_max = 0.0f;

    public ShotData getShotData();

    public EntityType getType();

    public String toString();
}

