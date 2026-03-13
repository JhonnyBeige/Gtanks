/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.timer.bonuses;

import gtanks.battles.BattlefieldModel;
import gtanks.commands.Type;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BonusesScheduler {
    private static final Timer TIMER = new Timer("BonusesScheduler timer");
    private static final HashMap<String, RemoveBonusTask> tasks = new HashMap();

    public static void runRemoveTask(BattlefieldModel bfModel, String bonusId, long disappearingTime) {
        RemoveBonusTask rbt = new RemoveBonusTask();
        rbt.bfModel = bfModel;
        rbt.bonusId = bonusId;
        tasks.put(bonusId, rbt);
        TIMER.schedule((TimerTask)rbt, disappearingTime * 1140L - 1000L);
    }

    static class RemoveBonusTask
    extends TimerTask {
        public String bonusId;
        public BattlefieldModel bfModel;

        RemoveBonusTask() {
        }

        @Override
        public void run() {
            if (this.bfModel == null) {
                return;
            }
            if (this.bfModel.activeBonuses == null) {
                return;
            }
            this.bfModel.activeBonuses.remove(this.bonusId);
            this.bfModel.sendToAllPlayers(Type.BATTLE, "remove_bonus", this.bonusId);
            tasks.remove(this.bonusId);
        }
    }
}

