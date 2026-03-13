/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;

public interface IWeapon {
    public void fire(String var1);

    public void quickFire(String var1);

    public void startFire(String var1);

    public void stopFire();

    public void onTarget(BattlefieldPlayerController[] var1, int var2);

    public IEntity getEntity();
}

