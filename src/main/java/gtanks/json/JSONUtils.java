package gtanks.json;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.chat.BattleChatMessage;
import gtanks.battles.ctf.CTFModel;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.maps.Map;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.mines.ServerMine;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.frezee.FrezeeEntity;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.battles.tanks.weapons.twins.TwinsEntity;
import gtanks.collections.FastHashMap;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.lobby.chat.ChatMessage;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.AutoEntryServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.Item;
import gtanks.users.garage.items.PropertyItem;
import gtanks.users.garage.items.modification.ModificationInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils {
    @ServicesInject(target=AutoEntryServices.class)
    private static AutoEntryServices autoEntryServices = AutoEntryServices.instance();
    @ServicesInject(target=AutoEntryServices.class)
    private static DatabaseManager databaseManager = DatabaseManagerImpl.instance();
    private static Object mod;
    private static JSONObject m;
    private static JSONArray prop;
    private static PropertyItem[] arrpropertyItem;
    private static int n3;
    private static int j;
    private static PropertyItem a;
    private static final String DOUBLE_CRYSTAL_EFFECT_ID = "double_crystalls";
    private static final boolean DOUBLE_CRYSTAL_FORCE_FOR_ALL = true;
    private static final int DOUBLE_CRYSTAL_FORCE_DAYS = 1;
    private static final boolean DOUBLE_CRYSTAL_RANDOM_BY_DEFAULT = false;
    private static final double DOUBLE_CRYSTAL_RANDOM_CHANCE = 0.05;
    private static final int DOUBLE_CRYSTAL_RANDOM_DAYS = 1;

    public static String parseConfiguratorEntity(Object entity, Class<?> clazz) {
        JSONObject jobj = new JSONObject();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                jobj.put(field.getName(), field.get(entity));
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return jobj.toJSONString();
    }

    public static String parseInitMinesComand(FastHashMap<BattlefieldPlayerController, ArrayList<ServerMine>> mines) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ArrayList<ServerMine> userMines : mines.values()) {
            for (ServerMine mine : userMines) {
                JSONObject _mine = new JSONObject();
                _mine.put("ownerId", mine.getOwner().tank.id);
                _mine.put("mineId", mine.getId());
                _mine.put("x", Float.valueOf(mine.getPosition().x));
                _mine.put("y", Float.valueOf(mine.getPosition().y));
                _mine.put("z", Float.valueOf(mine.getPosition().z));
                array.add(_mine);
            }
        }
        jobj.put("mines", array);
        return jobj.toJSONString();
    }

    public static String parsePutMineComand(ServerMine mine) {
        JSONObject jobj = new JSONObject();
        jobj.put("mineId", mine.getId());
        jobj.put("userId", mine.getOwner().tank.id);
        jobj.put("x", Float.valueOf(mine.getPosition().x));
        jobj.put("y", Float.valueOf(mine.getPosition().y));
        jobj.put("z", Float.valueOf(mine.getPosition().z));
        return jobj.toJSONString();
    }

    public static String parseInitInventoryComand(Garage garage) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        for (Item item : garage.getInventoryItems()) {
            JSONObject io = new JSONObject();
            if (item.id.equals("gift")) continue;
            io.put("id", item.id);
            io.put("count", item.count);
            io.put("slotId", item.index);
            io.put("itemEffectTime", item.id.equals("mine") ? 20 : (item.id.equals("health") ? 20 : 55));
            io.put("itemRestSec", 10);
            array.add(io);
        }
        jobj.put("items", array);
        return jobj.toJSONString();
    }

    public static String parseRemovePlayerComand(BattlefieldPlayerController player) {
        JSONObject jobj = new JSONObject();
        jobj.put("battleId", player.battle.battleInfo.battleId);
        jobj.put("id", player.getUser().getNickname());
        return jobj.toJSONString();
    }

    public static String parseRemovePlayerComand(String userId, String battleid) {
        JSONObject jobj = new JSONObject();
        jobj.put("battleId", battleid);
        jobj.put("id", userId);
        return jobj.toJSONString();
    }

    public static String parseAddPlayerComand(BattlefieldPlayerController player, BattleInfo battleInfo) {
        JSONObject obj = new JSONObject();
        obj.put("battleId", battleInfo.battleId);
        obj.put("id", player.getUser().getNickname());
        obj.put("kills", player.statistic.getScore());
        obj.put("name", player.getUser().getNickname());
        obj.put("rank", player.getUser().getRang() + 1);
        obj.put("type", player.playerTeamType);
        return obj.toJSONString();
    }

    public static String parseDropFlagCommand(FlagServer flag) {
        JSONObject obj = new JSONObject();
        obj.put("x", Float.valueOf(flag.position.x));
        obj.put("y", Float.valueOf(flag.position.y));
        obj.put("z", Float.valueOf(flag.position.z));
        obj.put("flagTeam", flag.flagTeamType);
        return obj.toJSONString();
    }

    public static String parseCTFModelData(BattlefieldModel model) {
        JSONObject obj = new JSONObject();
        CTFModel ctfModel = model.ctfModel;
        JSONObject basePosBlue = new JSONObject();
        basePosBlue.put("x", Float.valueOf(model.battleInfo.map.flagBluePosition.x));
        basePosBlue.put("y", Float.valueOf(model.battleInfo.map.flagBluePosition.y));
        basePosBlue.put("z", Float.valueOf(model.battleInfo.map.flagBluePosition.z));
        JSONObject basePosRed = new JSONObject();
        basePosRed.put("x", Float.valueOf(model.battleInfo.map.flagRedPosition.x));
        basePosRed.put("y", Float.valueOf(model.battleInfo.map.flagRedPosition.y));
        basePosRed.put("z", Float.valueOf(model.battleInfo.map.flagRedPosition.z));
        JSONObject posBlue = new JSONObject();
        posBlue.put("x", Float.valueOf(ctfModel.getBlueFlag().position.x));
        posBlue.put("y", Float.valueOf(ctfModel.getBlueFlag().position.y));
        posBlue.put("z", Float.valueOf(ctfModel.getBlueFlag().position.z));
        JSONObject posRed = new JSONObject();
        posRed.put("x", Float.valueOf(ctfModel.getRedFlag().position.x));
        posRed.put("y", Float.valueOf(ctfModel.getRedFlag().position.y));
        posRed.put("z", Float.valueOf(ctfModel.getRedFlag().position.z));
        obj.put("basePosBlueFlag", basePosBlue);
        obj.put("basePosRedFlag", basePosRed);
        obj.put("posBlueFlag", posBlue);
        obj.put("posRedFlag", posRed);
        obj.put("blueFlagCarrierId", ctfModel.getBlueFlag().owner == null ? null : ctfModel.getBlueFlag().owner.tank.id);
        obj.put("redFlagCarrierId", ctfModel.getRedFlag().owner == null ? null : ctfModel.getRedFlag().owner.tank.id);
        return obj.toJSONString();
    }

    public static String parseUpdateCoundPeoplesCommand(BattleInfo battle) {
        JSONObject obj = new JSONObject();
        obj.put("battleId", battle.battleId);
        obj.put("redPeople", battle.redPeople);
        obj.put("bluePeople", battle.bluePeople);
        return obj.toJSONString();
    }

    public static String parseFishishBattle(FastHashMap<String, BattlefieldPlayerController> players, int timeToRestart) {
        JSONObject obj = new JSONObject();
        JSONArray users = new JSONArray();
        obj.put("time_to_restart", timeToRestart);
        if (players == null) {
            return obj.toString();
        }
        for (BattlefieldPlayerController bpc : players.values()) {
            JSONObject stat = new JSONObject();
            stat.put("kills", bpc.statistic.getKills());
            stat.put("deaths", bpc.statistic.getDeaths());
            stat.put("id", bpc.getUser().getNickname());
            stat.put("rank", bpc.getUser().getRang() + 1);
            stat.put("prize", bpc.statistic.getPrize());
            stat.put("team_type", bpc.playerTeamType);
            stat.put("score", bpc.statistic.getScore());
            users.add(stat);
        }
        obj.put("users", users);
        return obj.toString();
    }

    public static String parsePlayerStatistic(BattlefieldPlayerController player) {
        JSONObject obj = new JSONObject();
        obj.put("kills", player.statistic.getKills());
        obj.put("deaths", player.statistic.getDeaths());
        obj.put("id", player.getUser().getNickname());
        obj.put("rank", player.getUser().getRang() + 1);
        obj.put("team_type", player.playerTeamType);
        obj.put("score", player.statistic.getScore());
        return obj.toString();
    }

    public static String parseSpawnCommand(BattlefieldPlayerController bpc, Vector3 pos) {
        JSONObject obj = new JSONObject();
        if (bpc == null || bpc.tank == null) {
            return null;
        }
        obj.put("tank_id", bpc.tank.id);
        obj.put("health", bpc.tank.health);
        obj.put("speed", Float.valueOf(bpc.tank.speed));
        obj.put("turn_speed", Float.valueOf(bpc.tank.turnSpeed));
        obj.put("turret_rotation_speed", Float.valueOf(bpc.tank.turretRotationSpeed));
        obj.put("incration_id", bpc.battle.incration);
        obj.put("team_type", bpc.playerTeamType);
        obj.put("x", Float.valueOf(pos.x));
        obj.put("y", Float.valueOf(pos.y));
        obj.put("z", Float.valueOf(pos.z));
        obj.put("rot", pos.rot);
        return obj.toString();
    }

    public static String parseBattleData(BattlefieldModel model) {
        JSONObject obj = new JSONObject();
        JSONArray users = new JSONArray();
        obj.put("name", model.battleInfo.name);
        obj.put("fund", model.tanksKillModel.getBattleFund());
        obj.put("scoreLimit", model.battleInfo.battleType.equals("CTF") ? model.battleInfo.numFlags : (model.battleInfo.battleType.equals("DOM") ? model.battleInfo.numFlags : model.battleInfo.numKills));
        obj.put("timeLimit", model.battleInfo.time);
        obj.put("currTime", model.getTimeLeft());
        obj.put("score_red", model.battleInfo.scoreRed);
        obj.put("score_blue", model.battleInfo.scoreBlue);
        obj.put("team", model.battleInfo.team);
        for (BattlefieldPlayerController bpc : model.players.values()) {
            JSONObject usr = new JSONObject();
            usr.put("nickname", bpc.parentLobby.getLocalUser().getNickname());
            usr.put("rank", bpc.parentLobby.getLocalUser().getRang() + 1);
            usr.put("teamType", bpc.playerTeamType);
            users.add(usr);
        }
        obj.put("users", users);
        return obj.toJSONString();
    }

    public static String parseUserToJSON(User user) {
        JSONUtils.applyDoubleCrystalSettings(user);
        JSONObject obj = new JSONObject();
        obj.put("name", user.getNickname());
        obj.put("have_double_crystalls", user.getGarage().hasActiveEffect(DOUBLE_CRYSTAL_EFFECT_ID));
        obj.put("crystall", user.getCrystall());
        obj.put("email", user.getEmail());
        obj.put("tester", user.getType() != TypeUser.DEFAULT);
        obj.put("next_score", user.getNextScore());
        obj.put("place", user.getPlace());
        obj.put("rang", user.getRang() + 1);
        obj.put("rating", user.getRating());
        obj.put("score", user.getScore());
        return obj.toJSONString();
    }

    private static void applyDoubleCrystalSettings(User user) {
        if (user == null || user.getGarage() == null) {
            return;
        }
        Garage garage = user.getGarage();
        boolean changed = false;
        if (DOUBLE_CRYSTAL_FORCE_FOR_ALL) {
            changed = garage.grantTimedEffect(DOUBLE_CRYSTAL_EFFECT_ID, DOUBLE_CRYSTAL_FORCE_DAYS) != null;
        } else if (DOUBLE_CRYSTAL_RANDOM_BY_DEFAULT && !garage.hasActiveEffect(DOUBLE_CRYSTAL_EFFECT_ID) && ThreadLocalRandom.current().nextDouble() < DOUBLE_CRYSTAL_RANDOM_CHANCE) {
            changed = garage.grantTimedEffect(DOUBLE_CRYSTAL_EFFECT_ID, DOUBLE_CRYSTAL_RANDOM_DAYS) != null;
        }
        if (changed) {
            garage.parseJSONData();
            databaseManager.update(garage);
        }
    }

    public static String parseUserEmailToJSON(User user) {
        JSONObject obj = new JSONObject();
        String userEmail = user.getEmail();
        obj.put("isComfirmEmail", userEmail != null && !userEmail.isEmpty());
        obj.put("emailNotice", userEmail == null);
        return obj.toJSONString();
    }

    public static String parseChatLobbyMessage(ChatMessage msg) {
        JSONObject obj = new JSONObject();
        obj.put("name", msg.user.getNickname());
        obj.put("rang", msg.user.getRang() + 1);
        obj.put("message", msg.message);
        obj.put("addressed", msg.addressed);
        obj.put("nameTo", msg.userTo == null ? "NULL" : msg.userTo.getNickname());
        obj.put("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.put("system", msg.system);
        obj.put("yellow", msg.yellowMessage);
        return obj.toJSONString();
    }

    public static JSONObject parseChatLobbyMessageObject(ChatMessage msg) {
        JSONObject obj = new JSONObject();
        obj.put("name", msg.user == null ? "" : msg.user.getNickname());
        obj.put("rang", msg.user == null ? 0 : msg.user.getRang() + 1);
        obj.put("message", msg.message);
        obj.put("addressed", msg.addressed);
        obj.put("nameTo", msg.userTo == null ? "" : msg.userTo.getNickname());
        obj.put("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.put("system", msg.system);
        obj.put("yellow", msg.yellowMessage);
        return obj;
    }

    public static String parseChatLobbyMessages(Collection<ChatMessage> messages) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ChatMessage msg : messages) {
            array.add(JSONUtils.parseChatLobbyMessageObject(msg));
        }
        obj.put("messages", array);
        return obj.toJSONString();
    }

    public static String parseGarageUser(User user) {
        try {
            JSONUtils.applyDoubleCrystalSettings(user);
            Garage garage = user.getGarage();
            JSONObject obj = new JSONObject();
            JSONArray array = new JSONArray();
            for (Item item : garage.items) {
                int n;
                int n2;
                Object[] arrobject;
                JSONObject i = new JSONObject();
                JSONArray properts = new JSONArray();
                JSONArray modification = new JSONArray();
                i.put("id", item.id);
                i.put("name", item.name.localizatedString(user.getLocalization()));
                i.put("description", item.description.localizatedString(user.getLocalization()));
                i.put("isInventory", JSONUtils.boolToString(item.isInventory));
                i.put("index", item.index);
                i.put("discount", JSONUtils.getDiscountForItemType(item.itemType, item.id));
                i.put("multicounted", item.multicounted);
                int value = Integer.parseInt(item.itemType.toString());
                i.put("type", value);
                i.put("modificationID", item.modificationIndex);
                i.put("next_price", item.nextPrice);
                i.put("next_rank", item.nextRankId);
                i.put("price", item.price);
                i.put("rank", item.rankId);
                i.put("count", item.count);
                if (item.propetys != null) {
                    arrobject = item.propetys;
                    n2 = item.propetys.length;
                    for (n = 0; n < n2; ++n) {
                        Object prop = arrobject[n];
                        if (prop == null || ((PropertyItem)prop).property == null) continue;
                        properts.add(JSONUtils.parseProperty((PropertyItem)prop));
                    }
                }
                if (item.modifications != null) {
                    arrobject = item.modifications;
                    n2 = item.modifications.length;
                    if (!(item.id.equals("hwthunder") || item.id.equals("titanxt") || item.id.equals("twinsxt") || item.id.equals("railgunxt") || item.id.equals("vikingxt") || item.id.equals("isidahw") || item.id.equals("smokyxt") || item.id.equals("frezeeny") || item.id.equals("flamethrowerhw") || item.id.equals("hunterhw") || item.id.equals("dictatorny") || item.id.equals("pumpkingun") || item.id.equals("mamonthw") || item.id.equals("bfg") || item.id.equals("praetorian") || item.id.equals("hornetxt"))) {
                        for (n = 0; n < n2; ++n) {
                            mod = arrobject[n];
                            m = new JSONObject();
                            prop = new JSONArray();
                            m.put("previewId", ((ModificationInfo)JSONUtils.mod).previewId);
                            m.put("price", ((ModificationInfo)JSONUtils.mod).price);
                            m.put("rank", ((ModificationInfo)JSONUtils.mod).rank);
                            if (((ModificationInfo)JSONUtils.mod).propertys != null) {
                                arrpropertyItem = ((ModificationInfo)JSONUtils.mod).propertys;
                                n3 = ((ModificationInfo)JSONUtils.mod).propertys.length;
                                for (j = 0; j < n3; ++j) {
                                    a = arrpropertyItem[j];
                                    if (a == null || JSONUtils.a.property == null) continue;
                                    prop.add(JSONUtils.parseProperty(a));
                                }
                            }
                            m.put("properts", prop);
                            modification.add(m);
                        }
                    } else {
                        for (n = 0; n < 1; ++n) {
                            mod = arrobject[n];
                            m = new JSONObject();
                            prop = new JSONArray();
                            m.put("previewId", ((ModificationInfo)JSONUtils.mod).previewId);
                            m.put("price", ((ModificationInfo)JSONUtils.mod).price);
                            m.put("rank", ((ModificationInfo)JSONUtils.mod).rank);
                            if (((ModificationInfo)JSONUtils.mod).propertys != null) {
                                arrpropertyItem = ((ModificationInfo)JSONUtils.mod).propertys;
                                n3 = ((ModificationInfo)JSONUtils.mod).propertys.length;
                                for (j = 0; j < n3; ++j) {
                                    a = arrpropertyItem[j];
                                    if (a == null || JSONUtils.a.property == null) continue;
                                    prop.add(JSONUtils.parseProperty(a));
                                }
                            }
                            m.put("properts", prop);
                            modification.add(m);
                        }
                    }
                }
                i.put("properts", properts);
                i.put("modification", modification);
                array.add(i);
            }
            obj.put("items", array);
            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static int getDiscountForItemType(ItemType itemType, String itemId) {
        if (itemId.equals("garland") || itemId.equals("gift")) {
            return 0;
        }
        switch (itemType) {
            case INVENTORY: {
                return 15;
            }
            case PLUGIN: {
                return 0;
            }
            case WEAPON:
            case ARMOR: {
                return 40;
            }
            case COLOR: {
                return 40;
            }
        }
        return 0;
    }

    public static String parseMarketItems(User user) {
        Garage garage = user.getGarage();
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        for (Item item : GarageItemsLoader.items.values()) {
            if (!garage.containsItem(item.id) && (!item.specialItem || GarageItemsLoader.canGroupUseSpecialItem(item.id, user.getType()))) {
                int n;
                int n2;
                Object[] arrobject;
                JSONObject i = new JSONObject();
                JSONArray properts = new JSONArray();
                JSONArray modification = new JSONArray();
                i.put("id", item.id);
                i.put("name", item.name.localizatedString(user.getLocalization()));
                i.put("description", item.description.localizatedString(user.getLocalization()));
                i.put("isInventory", item.isInventory);
                i.put("index", item.index);
                i.put("discount", JSONUtils.getDiscountForItemType(item.itemType, item.id));
                int value = Integer.parseInt(item.itemType.toString());
                i.put("type", value);
                i.put("modificationID", 0);
                i.put("next_price", item.nextPrice);
                i.put("next_rank", item.nextRankId);
                i.put("price", item.price);
                i.put("rank", item.rankId);
                if (item.propetys != null) {
                    arrobject = item.propetys;
                    n2 = item.propetys.length;
                    for (n = 0; n < n2; ++n) {
                        Object prop = arrobject[n];
                        properts.add(JSONUtils.parseProperty((PropertyItem)prop));
                    }
                }
                if (item.modifications != null) {
                    arrobject = item.modifications;
                    n2 = item.modifications.length;
                    if (!(item.id.equals("hwthunder") || item.id.equals("titanxt") || item.id.equals("twinsxt") || item.id.equals("railgunxt") || item.id.equals("vikingxt") || item.id.equals("smokyxt") || item.id.equals("frezeeny") || item.id.equals("isidahw") || item.id.equals("flamethrowerhw") || item.id.equals("dictatorny") || item.id.equals("pumpkingun") || item.id.equals("hunterhw") || item.id.equals("bfg") || item.id.equals("praetorian") || item.id.equals("mamonthw") || item.id.equals("hornetxt"))) {
                        for (n = 0; n < n2; ++n) {
                            mod = arrobject[n];
                            m = new JSONObject();
                            prop = new JSONArray();
                            m.put("previewId", ((ModificationInfo)JSONUtils.mod).previewId);
                            m.put("price", ((ModificationInfo)JSONUtils.mod).price);
                            m.put("rank", ((ModificationInfo)JSONUtils.mod).rank);
                            if (((ModificationInfo)JSONUtils.mod).propertys != null) {
                                arrpropertyItem = ((ModificationInfo)JSONUtils.mod).propertys;
                                n3 = ((ModificationInfo)JSONUtils.mod).propertys.length;
                                for (j = 0; j < n3; ++j) {
                                    a = arrpropertyItem[j];
                                    prop.add(JSONUtils.parseProperty(a));
                                }
                            }
                            m.put("properts", prop);
                            modification.add(m);
                        }
                    } else {
                        for (n = 0; n < 1; ++n) {
                            mod = arrobject[n];
                            m = new JSONObject();
                            prop = new JSONArray();
                            m.put("previewId", ((ModificationInfo)JSONUtils.mod).previewId);
                            m.put("price", ((ModificationInfo)JSONUtils.mod).price);
                            m.put("rank", ((ModificationInfo)JSONUtils.mod).rank);
                            if (((ModificationInfo)JSONUtils.mod).propertys != null) {
                                arrpropertyItem = ((ModificationInfo)JSONUtils.mod).propertys;
                                n3 = ((ModificationInfo)JSONUtils.mod).propertys.length;
                                for (j = 0; j < n3; ++j) {
                                    a = arrpropertyItem[j];
                                    prop.add(JSONUtils.parseProperty(a));
                                }
                            }
                            m.put("properts", prop);
                            modification.add(m);
                        }
                    }
                }
                i.put("properts", properts);
                i.put("modification", modification);
                jarray.add(i);
            }
            json.put("items", jarray);
        }
        return json.toString();
    }

    public static String parseItemInfo(Item item) {
        JSONObject obj = new JSONObject();
        obj.put("itemId", item.id);
        obj.put("addable", !item.id.equals("1000_scores"));
        obj.put("count", item.count);
        obj.put("multicounted", true);
        return obj.toJSONString();
    }

    public static String parseInitEffectsCommand(Garage garage) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        for (Item item : garage.getEffectItems()) {
            JSONObject io = new JSONObject();
            io.put("id", item.id + "_m0");
            io.put("time", item.timeRemaining);
            array.add(io);
        }
        jobj.put("effects", array);
        return jobj.toJSONString();
    }

    private static JSONObject parseProperty(PropertyItem item) {
        JSONObject h = new JSONObject();
        h.put("property", item.property.toString());
        h.put("value", item.value);
        return h;
    }

    public static String parseBattleMapList(User user) {
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        JSONArray jbattles = new JSONArray();
        for (Map map : MapsLoader.maps.values()) {
            JSONObject jmap = new JSONObject();
            jmap.put("id", map.id.replace(".xml", ""));
            jmap.put("name", map.name);
            jmap.put("gameName", "\u0442\u0438\u043f gameName");
            jmap.put("maxPeople", map.maxPlayers);
            jmap.put("maxRank", map.maxRank);
            jmap.put("minRank", map.minRank);
            jmap.put("themeName", map.themeId);
            jmap.put("skyboxId", map.skyboxId);
            jmap.put("dom", map.id.equals("map_sandbox") || map.id.equals("map_polygon") || map.id.startsWith("map_ring") || map.id.startsWith("map_factory") || map.id.equals("map_sandal") || map.id.equals("map_iran") || map.id.startsWith("map_rio") || map.id.startsWith("map_kungur") || map.id.startsWith("map_ny_2021"));
            jmap.put("ctf", map.ctf);
            jmap.put("tdm", map.tdm);
            jarray.add(jmap);
        }
        json.put("items", jarray);
        for (BattleInfo battle : BattlesList.getList()) {
            jbattles.add(JSONUtils.parseBattleInfo(battle, 1));
        }
        json.put("battles", jbattles);
        json.put("haveSubscribe", user.getGarage().containsItem("no_supplies"));
        Iterator<BattleInfo> iterator = BattlesList.getList().iterator();
        BattleInfo recommendedBattle = new BattleInfo();
        BattleInfo battle = null;
        while (iterator.hasNext()) {
            battle = iterator.next();
            if (battle.isPrivate) continue;
            if (battle.team) {
                if (recommendedBattle.team) {
                    if (battle.redPeople <= recommendedBattle.redPeople || battle.bluePeople <= recommendedBattle.bluePeople || battle.minRank <= user.getRang() || battle.bluePeople - battle.maxPeople >= 0 && battle.redPeople - battle.maxPeople >= 0 || battle.minRank <= user.getRang()) continue;
                    recommendedBattle = battle;
                    continue;
                }
                if (battle.redPeople <= recommendedBattle.countPeople || battle.bluePeople <= recommendedBattle.countPeople || battle.bluePeople - battle.maxPeople >= 0 && battle.redPeople - battle.maxPeople >= 0 || battle.minRank >= user.getRang()) continue;
                recommendedBattle = battle;
                continue;
            }
            if (recommendedBattle.team) {
                if (battle.countPeople <= recommendedBattle.redPeople || battle.countPeople <= recommendedBattle.bluePeople || battle.countPeople - battle.maxPeople >= 0) continue;
                recommendedBattle = battle;
                continue;
            }
            if (battle.countPeople <= recommendedBattle.countPeople || recommendedBattle.minRank <= user.getRang() || battle.countPeople - battle.maxPeople >= 0 || battle.minRank <= user.getRang()) continue;
            recommendedBattle = battle;
        }
        json.put("recommendedBattle", battle.battleId);
        return json.toString();
    }

    public static String parseBattleInfo(BattleInfo battle) {
        JSONObject json = new JSONObject();
        json.put("battleId", battle.battleId);
        json.put("mapId", battle.map.id);
        json.put("name", battle.name);
        json.put("previewId", battle.map.id + "_preview");
        json.put("team", battle.team);
        json.put("redPeople", battle.redPeople);
        json.put("bluePeople", battle.bluePeople);
        json.put("countPeople", battle.countPeople);
        json.put("maxPeople", battle.maxPeople);
        json.put("minRank", battle.minRank);
        json.put("maxRank", battle.maxRank);
        json.put("isPaid", battle.isPaid);
        return json.toJSONString();
    }

    public static JSONObject parseBattleInfo(BattleInfo battle, int i) {
        JSONObject json = new JSONObject();
        json.put("battleId", battle.battleId);
        json.put("mapId", battle.map.id);
        json.put("name", battle.name);
        json.put("previewId", battle.map.id + "_preview");
        json.put("team", battle.team);
        json.put("redPeople", battle.redPeople);
        json.put("bluePeople", battle.bluePeople);
        json.put("countPeople", battle.countPeople);
        json.put("maxPeople", battle.maxPeople);
        json.put("minRank", battle.minRank);
        json.put("maxRank", battle.maxRank);
        json.put("isPaid", battle.isPaid);
        return json;
    }

    public static String parseBattleInfoShow(BattleInfo battle, boolean spectator, User localUser) {
        JSONObject json = new JSONObject();
        if (battle == null) {
            json.put("null_battle", true);
            return json.toJSONString();
        }
        try {
            JSONArray users = new JSONArray();
            if (battle != null && battle.model != null && battle.model.players != null) {
                JSONObject obj_user;
                for (BattlefieldPlayerController battlefieldPlayerController : battle.model.players.values()) {
                    obj_user = new JSONObject();
                    obj_user.put("nickname", battlefieldPlayerController.parentLobby.getLocalUser().getNickname());
                    obj_user.put("rank", battlefieldPlayerController.parentLobby.getLocalUser().getRang() + 1);
                    obj_user.put("kills", battlefieldPlayerController.statistic.getKills());
                    obj_user.put("kills", battle.team ? battlefieldPlayerController.statistic.getScore() : battlefieldPlayerController.statistic.getKills());
                    obj_user.put("team_type", battlefieldPlayerController.playerTeamType);
                    users.add(obj_user);
                }
                for (AutoEntryServices.Data data : autoEntryServices.getPlayersByBattle(battle.model)) {
                    obj_user = new JSONObject();
                    User user = databaseManager.getUserById(data.userId);
                    obj_user.put("nickname", user.getNickname());
                    obj_user.put("rank", user.getRang() + 1);
                    obj_user.put("kills", battle.team ? data.statistic.getScore() : data.statistic.getKills());
                    obj_user.put("team_type", data.teamType);
                    users.add(obj_user);
                }
            }
            json.put("users_in_battle", users);
            json.put("name", battle.name);
            json.put("maxPeople", battle.maxPeople);
            json.put("type", battle.battleType);
            json.put("battleId", battle.battleId);
            json.put("minRank", battle.minRank);
            json.put("maxRank", battle.maxRank);
            json.put("timeLimit", battle.time);
            json.put("timeCurrent", battle.model.getTimeLeft());
            json.put("killsLimit", battle.numKills);
            if (battle.battleType.equals("CTF")) {
                json.put("killsLimit", battle.numFlags);
            }
            if (battle.battleType.equals("DOM")) {
                json.put("killsLimit", battle.numFlags);
            }
            json.put("scoreRed", battle.scoreRed);
            json.put("scoreBlue", battle.scoreBlue);
            json.put("autobalance", battle.autobalance);
            json.put("frielndyFie", battle.friendlyFire);
            json.put("paidBattle", battle.isPaid);
            json.put("withoutBonuses", battle.withoutBonuses);
            json.put("userAlreadyPaid", localUser.getGarage().containsItem("no_supplies"));
            json.put("fullCash", true);
            json.put("spectator", spectator);
            json.put("previewId", battle.map.id + "_preview");
        } catch (Exception ex) {
            ex.printStackTrace();
            return json.toString();
        }
        return json.toJSONString();
    }

    public static String parseBattleModelInfo(BattleInfo battle, boolean spectatorMode) {
        JSONObject json = new JSONObject();
        json.put("kick_period_ms", 300000);
        json.put("map_id", battle.map.id.replace(".xml", ""));
        json.put("invisible_time", 3500);
        json.put("skybox_id", battle.map.skyboxId);
        json.put("spectator", spectatorMode);
        json.put("sound_id", battle.map.mapTheme.getAmbientSoundId());
        json.put("game_mode", battle.map.mapTheme.getGameModeId());
        return json.toJSONString();
    }

    public static String parseTankData(BattlefieldModel player, BattlefieldPlayerController controller, Garage garageUser, Vector3 pos, boolean stateNull, int icration, String idTank, String nickname, int rank) {
        JSONObject json = new JSONObject();
        json.put("battleId", player.battleInfo.battleId);
        json.put("colormap_id", String.valueOf(garageUser.mountColormap.id) + "_m0");
        json.put("hull_id", String.valueOf(garageUser.mountHull.id) + "_m" + garageUser.mountHull.modificationIndex);
        json.put("turret_id", String.valueOf(garageUser.mountTurret.id) + "_m" + garageUser.mountTurret.modificationIndex);
        json.put("team_type", controller.playerTeamType);
        if (pos == null) {
            pos = new Vector3(0.0f, 0.0f, 0.0f);
        }
        json.put("position", String.valueOf(pos.x) + "@" + pos.y + "@" + pos.z + "@" + pos.rot);
        json.put("incration", icration);
        json.put("tank_id", idTank);
        json.put("nickname", nickname);
        json.put("state", controller.tank.state);
        json.put("turn_speed", Float.valueOf(controller.tank.getHull().turnSpeed));
        json.put("speed", Float.valueOf(controller.tank.getHull().speed));
        json.put("turret_turn_speed", Float.valueOf(controller.tank.turretRotationSpeed));
        json.put("health", controller.tank.health);
        json.put("rank", rank + 1);
        json.put("mass", Float.valueOf(controller.tank.getHull().mass));
        json.put("power", Float.valueOf(controller.tank.getHull().power));
        json.put("kickback", Float.valueOf(controller.tank.getWeapon().getEntity().getShotData().kickback));
        json.put("turret_rotation_accel", Float.valueOf(controller.tank.getWeapon().getEntity().getShotData().turretRotationAccel));
        json.put("impact_force", Float.valueOf(controller.tank.getWeapon().getEntity().getShotData().impactCoeff));
        json.put("state_null", stateNull);
        return json.toJSONString();
    }

    public static String parseMoveCommand(BattlefieldPlayerController player) {
        Tank tank = player.tank;
        JSONObject json = new JSONObject();
        JSONObject pos = new JSONObject();
        JSONObject orient = new JSONObject();
        JSONObject line = new JSONObject();
        JSONObject angle = new JSONObject();
        pos.put("x", Float.valueOf(tank.position.x));
        pos.put("y", Float.valueOf(tank.position.y));
        pos.put("z", Float.valueOf(tank.position.z));
        orient.put("x", Float.valueOf(tank.orientation.x));
        orient.put("y", Float.valueOf(tank.orientation.y));
        orient.put("z", Float.valueOf(tank.orientation.z));
        line.put("x", Float.valueOf(tank.linVel.x));
        line.put("y", Float.valueOf(tank.linVel.y));
        line.put("z", Float.valueOf(tank.linVel.z));
        angle.put("x", Float.valueOf(tank.angVel.x));
        angle.put("y", Float.valueOf(tank.angVel.y));
        angle.put("z", Float.valueOf(tank.angVel.z));
        json.put("position", pos);
        json.put("orient", orient);
        json.put("line", line);
        json.put("angle", angle);
        json.put("turretDir", tank.turretDir);
        json.put("ctrlBits", tank.controllBits);
        json.put("tank_id", tank.id);
        return json.toJSONString();
    }

    public static String parseBattleChatMessage(BattleChatMessage msg) {
        JSONObject jobj = new JSONObject();
        jobj.put("nickname", msg.nickname);
        jobj.put("rank", msg.rank + 1);
        jobj.put("message", msg.message);
        jobj.put("team_type", msg.teamType);
        jobj.put("system", msg.system);
        jobj.put("team", msg.team);
        return jobj.toJSONString();
    }

    public static String parseZoneInfo(Vector3 bonus, String type) {
        JSONObject jobj = new JSONObject();
        jobj.put("type", type);
        jobj.put("x", Float.valueOf(bonus.x));
        jobj.put("y", Float.valueOf(bonus.y));
        jobj.put("z", Float.valueOf(bonus.z));
        return jobj.toJSONString();
    }

    public static String parseBonusInfo(Bonus bonus, int inc, int disappearingTime) {
        JSONObject jobj = new JSONObject();
        jobj.put("id", String.valueOf(bonus.type.toString()) + "_" + inc);
        jobj.put("x", Float.valueOf(bonus.position.x));
        jobj.put("y", Float.valueOf(bonus.position.y));
        jobj.put("z", Float.valueOf(bonus.position.z));
        jobj.put("disappearing_time", disappearingTime);
        return jobj.toJSONString();
    }

    public static JSONObject parseSpecialEntity(IEntity entity) {
        JSONObject j = new JSONObject();
        switch (entity.getType()) {
            case FLAMETHROWER: {
                FlamethrowerEntity fm = (FlamethrowerEntity)entity;
                j.put("cooling_speed", fm.coolingSpeed);
                j.put("cone_angle", Float.valueOf(fm.coneAngle));
                j.put("heating_speed", fm.heatingSpeed);
                j.put("heat_limit", fm.heatLimit);
                j.put("range", Float.valueOf(fm.range));
                j.put("target_detection_interval", fm.targetDetectionInterval);
                break;
            }
            case TWINS: {
                TwinsEntity te = (TwinsEntity)entity;
                j.put("shot_radius", Float.valueOf(te.shotRadius));
                j.put("shot_range", Float.valueOf(te.shotRange));
                j.put("shot_speed", Float.valueOf(te.shotSpeed));
                break;
            }
            case ISIDA: {
                IsidaEntity ie = (IsidaEntity)entity;
                j.put("angle", Float.valueOf(ie.maxAngle));
                j.put("capacity", ie.capacity);
                j.put("chargeRate", ie.chargeRate);
                j.put("tickPeriod", ie.tickPeriod);
                j.put("coneAngle", Float.valueOf(ie.lockAngle));
                j.put("dischargeRate", ie.dischargeRate);
                j.put("radius", Float.valueOf(ie.maxRadius));
                break;
            }
            case THUNDER: {
                ThunderEntity the = (ThunderEntity)entity;
                j.put("impactForce", Float.valueOf(the.impactForce));
                j.put("maxSplashDamageRadius", Float.valueOf(the.maxSplashDamageRadius));
                j.put("minSplashDamagePercent", Float.valueOf(the.minSplashDamagePercent));
                j.put("minSplashDamageRadius", Float.valueOf(the.minSplashDamageRadius));
                break;
            }
            case FREEZE: {
                FrezeeEntity frezeeEntity = (FrezeeEntity)entity;
                j.put("damageAreaConeAngle", Float.valueOf(frezeeEntity.damageAreaConeAngle));
                j.put("damageAreaRange", Float.valueOf(frezeeEntity.damageAreaRange));
                j.put("energyCapacity", frezeeEntity.energyCapacity);
                j.put("energyRechargeSpeed", frezeeEntity.energyRechargeSpeed);
                j.put("energyDischargeSpeed", frezeeEntity.energyDischargeSpeed);
                j.put("weaponTickMsec", frezeeEntity.weaponTickMsec);
                break;
            }
            case RICOCHET: {
                RicochetEntity ricochetEntity = (RicochetEntity)entity;
                j.put("energyCapacity", ricochetEntity.energyCapacity);
                j.put("energyPerShot", ricochetEntity.energyPerShot);
                j.put("energyRechargeSpeed", Float.valueOf(ricochetEntity.energyRechargeSpeed));
                j.put("shotDistance", Float.valueOf(ricochetEntity.shotDistance));
                j.put("shotRadius", Float.valueOf(ricochetEntity.shotRadius));
                j.put("shotSpeed", Float.valueOf(ricochetEntity.shotSpeed));
                break;
            }
            case SNOWMAN: {
                SnowmanEntity se = (SnowmanEntity)entity;
                j.put("shot_radius", Float.valueOf(se.shotRadius));
                j.put("shot_range", Float.valueOf(se.shotRange));
                j.put("shot_speed", Float.valueOf(se.shotSpeed));
            }
        }
        return j;
    }

    public static String parseWeapons(Collection<IEntity> weapons, HashMap<String, WeaponWeakeningData> wwds) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (IEntity entity : weapons) {
            JSONObject weapon = new JSONObject();
            WeaponWeakeningData wwd = wwds.get(entity.getShotData().id);
            weapon.put("auto_aiming_down", entity.getShotData().autoAimingAngleDown);
            weapon.put("auto_aiming_up", entity.getShotData().autoAimingAngleUp);
            weapon.put("num_rays_down", entity.getShotData().numRaysDown);
            weapon.put("num_rays_up", entity.getShotData().numRaysUp);
            weapon.put("reload", entity.getShotData().reloadMsec);
            weapon.put("id", entity.getShotData().id);
            if (wwd != null) {
                weapon.put("max_damage_radius", wwd.maximumDamageRadius);
                weapon.put("min_damage_radius", wwd.minimumDamageRadius);
                weapon.put("min_damage_percent", wwd.minimumDamagePercent);
                weapon.put("has_wwd", true);
            } else {
                weapon.put("has_wwd", false);
            }
            weapon.put("special_entity", JSONUtils.parseSpecialEntity(entity));
            array.add(weapon);
        }
        obj.put("weapons", array);
        return obj.toJSONString();
    }

    public static String parseTankSpec(Tank tank, boolean notSmooth) {
        JSONObject obj = new JSONObject();
        obj.put("speed", Float.valueOf(tank.speed));
        obj.put("turnSpeed", Float.valueOf(tank.turnSpeed));
        obj.put("turretRotationSpeed", Float.valueOf(tank.turretRotationSpeed));
        obj.put("immediate", notSmooth);
        return obj.toString();
    }

    public static String boolToString(boolean src) {
        return src ? "true" : "false";
    }
}
