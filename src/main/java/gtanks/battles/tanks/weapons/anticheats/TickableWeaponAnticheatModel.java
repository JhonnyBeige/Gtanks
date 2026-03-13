/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.anticheats;

import gtanks.battles.anticheats.AnticheatModel;

@AnticheatModel(name="TickableWeaponAnticheatModel", actionInfo="\u0421\u0440\u0430\u0432\u043d\u0438\u0432\u0430\u0435\u0442 \u043f\u0435\u0440\u0438\u043e\u0434 \u0442\u0438\u043a\u043e\u0432 \u0442\u0430\u043a\u0438\u0445 \u043f\u0443\u0448\u0435\u043a \u043a\u0430\u043a: \u041e\u0433\u043d\u0451\u043c\u0435\u0442, \u0424\u0440\u0438\u0437, \u0418\u0437\u0438\u0434\u0430")
public class TickableWeaponAnticheatModel {
    private int normalTickTime;

    public TickableWeaponAnticheatModel(int normalTickTime) {
        this.normalTickTime = normalTickTime;
    }

    public boolean check(int timeFromClient) {
        return this.normalTickTime == timeFromClient;
    }
}

