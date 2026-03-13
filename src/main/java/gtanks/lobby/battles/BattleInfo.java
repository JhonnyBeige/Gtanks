/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.lobby.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.maps.Map;
import java.util.Date;

public class BattleInfo {
    public boolean unremoveable;
    public String battleCreator;
    public Date createdTime;
    public boolean withoutBonuses;
    public String battleId;
    public Map map;
    public String battleType = "DM";
    public String name;
    public boolean team;
    public int redPeople;
    public int bluePeople;
    public int countPeople;
    public int maxPeople;
    public int minRank;
    public int maxRank;
    public boolean isPaid;
    public boolean isPrivate;
    public int time;
    public int numKills;
    public int numFlags;
    public boolean friendlyFire;
    public int scoreBlue = 0;
    public int scoreRed = 0;
    public boolean autobalance;
    public boolean inventory;
    public BattlefieldModel model;

    public String toString() {
        return "{" + this.name + " | " + this.battleId + "}";
    }

    public String dump() {
        return "Battle dump:\nName: " + this.name + "\nCreator: " + this.battleCreator + "\nCreated time: " + String.valueOf(this.createdTime);
    }
}

