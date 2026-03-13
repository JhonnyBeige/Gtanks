/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.railgun;

import gtanks.battles.tanks.weapons.EntityType;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class RailgunEntity
implements IEntity {
    public int chargingTime;
    public int weakeningCoeff;
    public float damage_min;
    public float damage_max;
    private ShotData shotData;
    public final EntityType type = EntityType.RAILGUN;

    public RailgunEntity(ShotData shotData, int charingTime, int weakeningCoeff, float damage_min, float damage_max) {
        this.chargingTime = charingTime;
        this.weakeningCoeff = weakeningCoeff;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.shotData = shotData;
    }

    @Override
    public String toString() {
        return "chargingTime: " + this.chargingTime + "\nweakeningCoeff: " + this.weakeningCoeff + "\ndamage_min: " + this.damage_min + "\ndamage_max: " + this.damage_max;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return this.type;
    }
}

