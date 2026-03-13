/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system;

import gtanks.battles.maps.MapsLoader;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.system.BattlesGC;

public class SystemBattlesHandler {
    public static BattleInfo newbieBattleToEnter;

    public static void systemBattlesInit() {
        SystemBattlesHandler.newbiesMapConfigSetup();
        SystemBattlesHandler.middleMapConfigSetup();
        SystemBattlesHandler.noLimitMapConfigSetup();
    }

    public static void newbiesMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "DM";
        battle.team = false;
        battle.numKills = 10;
        battle.minRank = 1;
        battle.maxRank = 8;
        battle.isPaid = false;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = true;
        battle.name = "For newbies DM";
        battle.map = MapsLoader.maps.get("map_ny_2021");
        battle.maxPeople = 12;
        battle.autobalance = false;
        battle.time = 600;
        BattlesList.tryCreateBatle(battle);
        BattlesGC.cancelRemoving(battle.model);
        newbieBattleToEnter = BattlesList.getBattleInfoById(battle.battleId);
    }

    public static void middleMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "DM";
        battle.team = false;
        battle.numKills = 100;
        battle.minRank = 8;
        battle.maxRank = 17;
        battle.isPaid = false;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = true;
        battle.name = "For middle DM";
        battle.map = MapsLoader.maps.get("map_desert");
        battle.maxPeople = 15;
        battle.autobalance = false;
        battle.time = 2100;
        BattlesList.tryCreateBatle(battle);
        BattlesGC.cancelRemoving(battle.model);
    }

    public static void noLimitMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "DM";
        battle.team = false;
        battle.numKills = 300;
        battle.minRank = 0;
        battle.maxRank = 27;
        battle.isPaid = false;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = true;
        battle.name = "For All DM";
        battle.map = MapsLoader.maps.get("map_highway");
        battle.maxPeople = 20;
        battle.autobalance = false;
        battle.time = 4000;
        BattlesList.tryCreateBatle(battle);
        BattlesGC.cancelRemoving(battle.model);
    }
}

