/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.effects;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.math.Vector3;

public interface Effect {
    public void activate(BattlefieldPlayerController var1, boolean var2, Vector3 var3);

    public void deactivate();

    public EffectType getEffectType();

    public int getID();

    public int getDurationTime();
}

