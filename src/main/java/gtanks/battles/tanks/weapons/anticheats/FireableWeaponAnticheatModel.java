/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.anticheats;

import gtanks.battles.anticheats.AnticheatModel;

@AnticheatModel(name="FireableWeaponAnticheatModel", actionInfo="\u0421\u0440\u0430\u0432\u043d\u0438\u0432\u0430\u0435\u0442 \u0432\u0440\u0435\u043c\u044f \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438 \u0442\u0430\u043a\u0438\u0445 \u043f\u0443\u0448\u0435\u043a \u043a\u0430\u043a: \u0421\u043c\u043e\u043a\u0438, \u0422\u0432\u0438\u043d\u0441, \u0420\u0435\u043b\u044c\u0441\u0430, \u0413\u0440\u043e\u043c, \u0420\u0438\u043a\u043e\u0448\u0435\u0442")
public class FireableWeaponAnticheatModel {
    private int normalReloadTime;

    public FireableWeaponAnticheatModel(int normalReloadTime) {
        this.normalReloadTime = normalReloadTime;
    }

    public boolean check(int reloadTimeFromClient) {
        return this.normalReloadTime == reloadTimeFromClient;
    }
}

