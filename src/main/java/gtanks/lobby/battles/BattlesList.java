/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.lobby.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.services.LobbysServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.locations.UserLocation;
import gtanks.utils.StringUtils;
import java.util.ArrayList;
import java.util.Random;

public class BattlesList {
    private static ArrayList<BattleInfo> battles = new ArrayList();
    private static int countBattles = 0;
    @ServicesInject(target=LobbysServices.class)
    private static LobbysServices lobbysServices = LobbysServices.getInstance();
    private static BattlefieldModel model;

    public static boolean tryCreateBatle(BattleInfo btl) {
        btl.battleId = BattlesList.generateId(btl.name, btl.map.id);
        if (BattlesList.getBattleInfoById(btl.battleId) != null) {
            return false;
        }
        battles.add(btl);
        ++countBattles;
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "create_battle", JSONUtils.parseBattleInfo(btl));
        btl.model = BattlesList.setModel(new BattlefieldModel(btl));
        return true;
    }

    public static void removeBattle(BattleInfo battle) {
        if (battle == null) {
            return;
        }
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("remove_battle", ";", battle.battleId));
        if (battle.model != null && battle.model.players != null) {
            for (BattlefieldPlayerController player : battle.model.players.values()) {
                player.parentLobby.kick();
            }
        }
        battle.model.destroy();
        battles.remove(battle);
    }

    public static ArrayList<BattleInfo> getList() {
        return battles;
    }

    private static String generateId(String gameName, String mapId) {
        String id = new Random().nextInt(50000) + "@" + gameName + "@#" + countBattles;
        return id;
    }

    public static BattleInfo getBattleInfoById(String id) {
        for (BattleInfo battle : battles) {
            if (!battle.battleId.equals(id)) continue;
            return battle;
        }
        return null;
    }

    public static BattlefieldModel getModel() {
        return model;
    }

    public static BattlefieldModel setModel(BattlefieldModel model) {
        BattlesList.model = model;
        return model;
    }
}

