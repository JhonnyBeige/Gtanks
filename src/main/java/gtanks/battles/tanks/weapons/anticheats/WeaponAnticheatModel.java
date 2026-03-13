/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.anticheats;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.logger.statistic.CheatDetectedLogger;
import java.util.ArrayList;

@Deprecated
@AnticheatModel(name="AnticheatModel", actionInfo="\u0421\u0440\u0430\u0432\u043d\u0438\u0432\u0430\u0435\u0442 \u0432\u0440\u0435\u043c\u044f \u043f\u0440\u043e\u0448\u043b\u043e\u0433\u043e \u0432\u044b\u0441\u0442\u0440\u0435\u043b\u0430, \u0435\u0441\u043b\u0438 \u043e\u043d\u043e \u043c\u0435\u043d\u044c\u0448\u0435 \u0447\u0435\u043c \u043d\u043e\u0440\u043c\u0430 - \u0447\u0438\u0442\u0435\u0440.")
public class WeaponAnticheatModel {
    private int timeReloadWeapon;
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private ArrayList<Long> suspiciousDeltas;
    private long lastFireTime;

    public WeaponAnticheatModel(int timeReloadWeapon, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        this.timeReloadWeapon = timeReloadWeapon;
        this.bfModel = bfModel;
        this.player = player;
        this.suspiciousDeltas = new ArrayList();
    }

    public boolean onFire() {
        long delta = System.currentTimeMillis() - this.lastFireTime;
        if (delta <= (long)this.timeReloadWeapon) {
            if (this.suspiciousDeltas.size() >= 7) {
                CheatDetectedLogger.cheatDetected(this.player.getUser().getNickname(), this.getClass(), this.player.tank.getWeapon().getClass(), this.timeReloadWeapon, this.deltasToString());
                this.bfModel.cheatDetected(this.player, this.getClass());
            }
            this.suspiciousDeltas.add(delta);
            this.lastFireTime = System.currentTimeMillis();
            return true;
        }
        this.lastFireTime = System.currentTimeMillis();
        return false;
    }

    public boolean onStartFire() {
        return false;
    }

    public boolean onStopFire() {
        return false;
    }

    private String deltasToString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.suspiciousDeltas.size(); ++i) {
            sb.append(String.valueOf(this.suspiciousDeltas.get(i)));
            if (i == this.suspiciousDeltas.size() - 1) continue;
            sb.append(", ");
        }
        return sb.toString();
    }
}

