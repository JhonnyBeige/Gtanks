/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons;

import gtanks.battles.tanks.Tank;

public class WeaponUtils {
    public static int calculateHealth(Tank tank, float damage) {
        float _damage = 10000.0f / (tank.getHull().hp / damage);
        return (int)_damage;
    }

    public static float calculateDamageFromDistance(float damage, int procent) {
        return damage - damage * (float)(procent / 100);
    }

    public static float calculateDamageWithResistance(float damage, int resistancePercent) {
        return damage - damage / 100.0f * (float)resistancePercent;
    }
}

