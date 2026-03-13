/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system;

import gtanks.battles.BattlefieldModel;
import gtanks.lobby.battles.BattlesList;
import gtanks.logger.Logger;
import gtanks.services.AutoEntryServices;
import gtanks.services.annotations.ServicesInject;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BattlesGC {
    private static final long TIME_FOR_REMOVING_EMPTY_BATTLE = 20000L;
    private static HashMap<BattlefieldModel, Timer> battlesForRemove = new HashMap();
    @ServicesInject(target=AutoEntryServices.class)
    private static AutoEntryServices autoEntryServices = AutoEntryServices.instance();

    public static void addBattleForRemove(BattlefieldModel battle) {
        if (battle == null) {
            return;
        }
        if (battle.battleInfo.unremoveable) {
            return;
        }
        Timer timer = new Timer("BattlesGC::Timer for battle: " + battle.battleInfo.battleId);
        timer.schedule((TimerTask)new RemoveBattleTask(battle), 20000L);
        battlesForRemove.put(battle, timer);
    }

    public static void cancelRemoving(BattlefieldModel model) {
        Timer timer = battlesForRemove.get(model);
        if (timer == null) {
            return;
        }
        timer.cancel();
        battlesForRemove.remove(model);
    }

    private static void removeEmptyBattle(BattlefieldModel battle) {
        Logger.log("[BattlesGarbageCollector]: battle[" + String.valueOf(battle.battleInfo) + "] has been deleted by inactivity.");
        BattlesList.removeBattle(battle.battleInfo);
        autoEntryServices.battleDisposed(battle);
    }

    static class RemoveBattleTask
    extends TimerTask {
        private BattlefieldModel battle;

        public RemoveBattleTask(BattlefieldModel battle) {
            this.battle = battle;
        }

        @Override
        public void run() {
            if (this.battle != null) {
                BattlesGC.removeEmptyBattle(this.battle);
            }
        }
    }
}

